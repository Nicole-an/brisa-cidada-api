package br.com.brisacidada.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import br.com.brisacidada.api.config.HeatAlertProperties;
import br.com.brisacidada.api.config.ModelMapperConfig;
import br.com.brisacidada.api.dto.response.AlertLevel;
import br.com.brisacidada.api.entity.HeatZone;
import br.com.brisacidada.api.entity.ReadingQuality;
import br.com.brisacidada.api.entity.ReadingSource;
import br.com.brisacidada.api.entity.RiskLevel;
import br.com.brisacidada.api.entity.TemperatureReading;
import br.com.brisacidada.api.mapper.CoolingPointMapper;
import br.com.brisacidada.api.repository.CoolingPointRepository;
import br.com.brisacidada.api.repository.TemperatureReadingRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeatAlertServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-15T15:00:00Z");

    @Mock
    private HeatZoneService heatZoneService;
    @Mock
    private TemperatureReadingRepository readingRepository;
    @Mock
    private CoolingPointRepository coolingPointRepository;

    private HeatAlertService service;

    @BeforeEach
    void setUp() {
        HeatAlertProperties properties = new HeatAlertProperties();
        service = new HeatAlertService(heatZoneService, readingRepository, coolingPointRepository,
                new CoolingPointMapper(new ModelMapperConfig().modelMapper()), properties,
                Clock.fixed(NOW, ZoneOffset.UTC));

        HeatZone zone = new HeatZone();
        zone.setId(1L);
        zone.setName("Centro");
        zone.setRiskLevel(RiskLevel.HIGH);
        lenient().when(heatZoneService.getEntity(1L)).thenReturn(zone);
        lenient().when(coolingPointRepository.findByHeatZoneIdAndActiveTrue(eq(1L), any())).thenReturn(List.of());
    }

    @Test
    void withoutReadingsReturnsInsufficientData() {
        when(readingRepository.findBySensorHeatZoneIdAndMeasuredAtBetweenAndQualityNot(
                1L, NOW.minusSeconds(3 * 3600), NOW, ReadingQuality.INVALID)).thenReturn(List.of());

        var alert = service.evaluate(1L);

        assertThat(alert.alertLevel()).isEqualTo(AlertLevel.INSUFFICIENT_DATA);
        assertThat(alert.readingsConsidered()).isZero();
        assertThat(alert.maxHeatIndexCelsius()).isNull();
        assertThat(alert.disclaimer()).contains("Nenhuma notificação");
    }

    @Test
    void hotAndHumidReadingsProduceDangerAndFlagSimulatedData() {
        when(readingRepository.findBySensorHeatZoneIdAndMeasuredAtBetweenAndQualityNot(
                1L, NOW.minusSeconds(3 * 3600), NOW, ReadingQuality.INVALID)).thenReturn(List.of(
                reading("30.00", "50.00", ReadingSource.SENSOR, NOW.minusSeconds(3000)),
                reading("35.00", "60.00", ReadingSource.SIMULATED, NOW.minusSeconds(600))));

        var alert = service.evaluate(1L);

        assertThat(alert.alertLevel()).isEqualTo(AlertLevel.DANGER);
        assertThat(alert.readingsConsidered()).isEqualTo(2);
        assertThat(alert.maxTemperatureCelsius()).isEqualByComparingTo("35.0");
        assertThat(alert.averageTemperatureCelsius()).isEqualByComparingTo("32.5");
        assertThat(alert.latestReadingAt()).isEqualTo(NOW.minusSeconds(600));
        assertThat(alert.includesSimulatedData()).isTrue();
        assertThat(alert.recommendations()).isNotEmpty();
    }

    @Test
    void classifyUsesConfiguredThresholds() {
        assertThat(service.classify(new BigDecimal("26.9"))).isEqualTo(AlertLevel.NORMAL);
        assertThat(service.classify(new BigDecimal("27"))).isEqualTo(AlertLevel.ATTENTION);
        assertThat(service.classify(new BigDecimal("32"))).isEqualTo(AlertLevel.ALERT);
        assertThat(service.classify(new BigDecimal("41"))).isEqualTo(AlertLevel.DANGER);
        assertThat(service.classify(new BigDecimal("54"))).isEqualTo(AlertLevel.EXTREME);
    }

    @Test
    void heatIndexMatchesNwsReferenceValues() {
        // Tabela NWS: 90 °F e 60% UR ≈ 100 °F (37,8 °C).
        assertThat(HeatIndexCalculator.heatIndexCelsius(32.2222, 60)).isCloseTo(37.6, within(0.5));
        // Abaixo de ~80 °F usa a fórmula simplificada: sensação próxima da temperatura.
        assertThat(HeatIndexCalculator.heatIndexCelsius(25, 50)).isCloseTo(24.9, within(0.3));
    }

    private static TemperatureReading reading(String temperature, String humidity, ReadingSource source,
                                              Instant measuredAt) {
        TemperatureReading reading = new TemperatureReading();
        reading.setTemperatureCelsius(new BigDecimal(temperature));
        reading.setHumidityPercent(new BigDecimal(humidity));
        reading.setSource(source);
        reading.setQuality(ReadingQuality.VALID);
        reading.setMeasuredAt(measuredAt);
        return reading;
    }
}
