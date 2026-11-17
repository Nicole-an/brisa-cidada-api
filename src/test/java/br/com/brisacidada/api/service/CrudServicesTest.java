package br.com.brisacidada.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.brisacidada.api.config.ModelMapperConfig;
import br.com.brisacidada.api.dto.request.CoolingPointRequest;
import br.com.brisacidada.api.dto.request.HeatZoneRequest;
import br.com.brisacidada.api.dto.request.SensorRequest;
import br.com.brisacidada.api.dto.request.TemperatureReadingRequest;
import br.com.brisacidada.api.entity.CoolingPoint;
import br.com.brisacidada.api.entity.HeatZone;
import br.com.brisacidada.api.entity.ReadingQuality;
import br.com.brisacidada.api.entity.ReadingSource;
import br.com.brisacidada.api.entity.RiskLevel;
import br.com.brisacidada.api.entity.Sensor;
import br.com.brisacidada.api.entity.SensorStatus;
import br.com.brisacidada.api.entity.TemperatureReading;
import br.com.brisacidada.api.exception.ConflictException;
import br.com.brisacidada.api.exception.ResourceNotFoundException;
import br.com.brisacidada.api.mapper.CoolingPointMapper;
import br.com.brisacidada.api.mapper.HeatZoneMapper;
import br.com.brisacidada.api.mapper.SensorMapper;
import br.com.brisacidada.api.mapper.TemperatureReadingMapper;
import br.com.brisacidada.api.repository.CoolingPointRepository;
import br.com.brisacidada.api.repository.HeatZoneRepository;
import br.com.brisacidada.api.repository.SensorRepository;
import br.com.brisacidada.api.repository.TemperatureReadingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class CrudServicesTest {

    private final ModelMapper modelMapper = new ModelMapperConfig().modelMapper();

    @Mock
    private HeatZoneRepository heatZoneRepository;
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private TemperatureReadingRepository readingRepository;
    @Mock
    private CoolingPointRepository coolingPointRepository;

    private HeatZoneService heatZoneService;
    private SensorService sensorService;
    private TemperatureReadingService readingService;
    private CoolingPointService coolingPointService;

    @BeforeEach
    void setUp() {
        heatZoneService = new HeatZoneService(heatZoneRepository, sensorRepository, coolingPointRepository,
                new HeatZoneMapper(modelMapper));
        sensorService = new SensorService(sensorRepository, readingRepository, heatZoneService,
                new SensorMapper(modelMapper));
        readingService = new TemperatureReadingService(readingRepository, sensorService,
                new TemperatureReadingMapper(modelMapper));
        coolingPointService = new CoolingPointService(coolingPointRepository, heatZoneService,
                new CoolingPointMapper(modelMapper));
    }

    @Test
    void createHeatZoneDefaultsActiveToTrue() {
        when(heatZoneRepository.existsByNameIgnoreCaseAndCityIgnoreCase("Zona", "SP")).thenReturn(false);
        when(heatZoneRepository.saveAndFlush(any(HeatZone.class))).thenAnswer(inv -> {
            HeatZone zone = inv.getArgument(0);
            zone.setId(1L);
            return zone;
        });

        var response = heatZoneService.create(zoneRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getActive()).isTrue();
        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    void createHeatZoneWithDuplicatedNameAndCityThrowsConflict() {
        when(heatZoneRepository.existsByNameIgnoreCaseAndCityIgnoreCase("Zona", "SP")).thenReturn(true);

        assertThatThrownBy(() -> heatZoneService.create(zoneRequest())).isInstanceOf(ConflictException.class);
        verify(heatZoneRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteHeatZoneWithSensorsThrowsConflict() {
        when(heatZoneRepository.findById(1L)).thenReturn(Optional.of(zone(1L)));
        when(sensorRepository.existsByHeatZoneId(1L)).thenReturn(true);

        assertThatThrownBy(() -> heatZoneService.delete(1L)).isInstanceOf(ConflictException.class);
        verify(heatZoneRepository, never()).delete(any());
    }

    @Test
    void findMissingHeatZoneThrowsNotFound() {
        when(heatZoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> heatZoneService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createSensorForMissingZoneThrowsNotFound() {
        when(heatZoneRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensorService.create(sensorRequest(5L))).isInstanceOf(ResourceNotFoundException.class);
        verify(sensorRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSensorWithDuplicatedCodeThrowsConflict() {
        when(heatZoneRepository.findById(1L)).thenReturn(Optional.of(zone(1L)));
        when(sensorRepository.existsByCodeIgnoreCase("SP-001")).thenReturn(true);

        assertThatThrownBy(() -> sensorService.create(sensorRequest(1L))).isInstanceOf(ConflictException.class);
    }

    @Test
    void updateSensorMovesItToAnotherZoneWithoutChangingZoneIds() {
        HeatZone original = zone(1L);
        HeatZone target = zone(2L);
        Sensor sensor = new Sensor();
        sensor.setId(10L);
        sensor.setHeatZone(original);
        when(sensorRepository.findById(10L)).thenReturn(Optional.of(sensor));
        when(heatZoneRepository.findById(2L)).thenReturn(Optional.of(target));
        when(sensorRepository.saveAndFlush(sensor)).thenReturn(sensor);

        var response = sensorService.update(10L, sensorRequest(2L));

        assertThat(response.getHeatZoneId()).isEqualTo(2L);
        assertThat(original.getId()).isEqualTo(1L);
        assertThat(sensor.getCode()).isEqualTo("SP-001");
    }

    @Test
    void deleteSensorWithReadingsThrowsConflict() {
        Sensor sensor = new Sensor();
        sensor.setId(10L);
        when(sensorRepository.findById(10L)).thenReturn(Optional.of(sensor));
        when(readingRepository.existsBySensorId(10L)).thenReturn(true);

        assertThatThrownBy(() -> sensorService.delete(10L)).isInstanceOf(ConflictException.class);
    }

    @Test
    void createReadingDefaultsQualityAndUpdatesSensorLastCommunication() {
        Sensor sensor = new Sensor();
        sensor.setId(10L);
        sensor.setCode("SP-001");
        sensor.setLastCommunicationAt(Instant.parse("2026-01-01T10:00:00Z"));
        when(sensorRepository.findById(10L)).thenReturn(Optional.of(sensor));
        when(readingRepository.saveAndFlush(any(TemperatureReading.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant measuredAt = Instant.parse("2026-01-01T12:00:00Z");
        var response = readingService.create(readingRequest(10L, measuredAt));

        assertThat(response.getQuality()).isEqualTo(ReadingQuality.VALID);
        assertThat(response.getSensorCode()).isEqualTo("SP-001");
        assertThat(sensor.getLastCommunicationAt()).isEqualTo(measuredAt);
    }

    @Test
    void olderReadingDoesNotMoveLastCommunicationBackwards() {
        Instant latest = Instant.parse("2026-01-01T12:00:00Z");
        Sensor sensor = new Sensor();
        sensor.setId(10L);
        sensor.setLastCommunicationAt(latest);
        when(sensorRepository.findById(10L)).thenReturn(Optional.of(sensor));
        when(readingRepository.saveAndFlush(any(TemperatureReading.class))).thenAnswer(inv -> inv.getArgument(0));

        readingService.create(readingRequest(10L, Instant.parse("2026-01-01T08:00:00Z")));

        assertThat(sensor.getLastCommunicationAt()).isEqualTo(latest);
    }

    @Test
    void duplicatedReadingThrowsConflict() {
        Sensor sensor = new Sensor();
        sensor.setId(10L);
        Instant measuredAt = Instant.parse("2026-01-01T12:00:00Z");
        when(sensorRepository.findById(10L)).thenReturn(Optional.of(sensor));
        when(readingRepository.existsBySensorIdAndMeasuredAt(10L, measuredAt)).thenReturn(true);

        assertThatThrownBy(() -> readingService.create(readingRequest(10L, measuredAt)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void coolingPointWithoutZoneIsCreatedUnlinked() {
        when(coolingPointRepository.saveAndFlush(any(CoolingPoint.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = coolingPointService.create(coolingPointRequest(null));

        assertThat(response.getHeatZoneId()).isNull();
        assertThat(response.getActive()).isTrue();
    }

    @Test
    void coolingPointWithMissingZoneThrowsNotFound() {
        when(heatZoneRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coolingPointService.create(coolingPointRequest(7L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static HeatZone zone(Long id) {
        HeatZone zone = new HeatZone();
        zone.setId(id);
        zone.setName("Zona " + id);
        zone.setRiskLevel(RiskLevel.HIGH);
        return zone;
    }

    private static HeatZoneRequest zoneRequest() {
        HeatZoneRequest request = new HeatZoneRequest();
        request.setName("Zona");
        request.setNeighborhood("Centro");
        request.setCity("SP");
        request.setLatitude(new BigDecimal("-23.55"));
        request.setLongitude(new BigDecimal("-46.63"));
        request.setRiskLevel(RiskLevel.HIGH);
        return request;
    }

    private static SensorRequest sensorRequest(Long zoneId) {
        SensorRequest request = new SensorRequest();
        request.setCode("SP-001");
        request.setName("Sensor");
        request.setLatitude(new BigDecimal("-23.55"));
        request.setLongitude(new BigDecimal("-46.63"));
        request.setStatus(SensorStatus.ACTIVE);
        request.setInstallationDate(LocalDate.of(2025, 1, 1));
        request.setHeatZoneId(zoneId);
        return request;
    }

    private static TemperatureReadingRequest readingRequest(Long sensorId, Instant measuredAt) {
        TemperatureReadingRequest request = new TemperatureReadingRequest();
        request.setSensorId(sensorId);
        request.setMeasuredAt(measuredAt);
        request.setTemperatureCelsius(new BigDecimal("33.50"));
        request.setHumidityPercent(new BigDecimal("50.00"));
        request.setSource(ReadingSource.SENSOR);
        return request;
    }

    private static CoolingPointRequest coolingPointRequest(Long zoneId) {
        CoolingPointRequest request = new CoolingPointRequest();
        request.setName("Biblioteca");
        request.setAddress("Rua A, 1");
        request.setNeighborhood("Centro");
        request.setLatitude(new BigDecimal("-23.55"));
        request.setLongitude(new BigDecimal("-46.63"));
        request.setOpeningHours("08:00-18:00");
        request.setCapacity(30);
        request.setHeatZoneId(zoneId);
        return request;
    }
}
