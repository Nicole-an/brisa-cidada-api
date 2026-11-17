package br.com.brisacidada.api.service;

import br.com.brisacidada.api.config.HeatAlertProperties;
import br.com.brisacidada.api.dto.response.AlertLevel;
import br.com.brisacidada.api.dto.response.CoolingPointResponse;
import br.com.brisacidada.api.dto.response.HeatAlertResponse;
import br.com.brisacidada.api.entity.HeatZone;
import br.com.brisacidada.api.entity.ReadingQuality;
import br.com.brisacidada.api.entity.ReadingSource;
import br.com.brisacidada.api.entity.TemperatureReading;
import br.com.brisacidada.api.mapper.CoolingPointMapper;
import br.com.brisacidada.api.repository.CoolingPointRepository;
import br.com.brisacidada.api.repository.TemperatureReadingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeatAlertService {

    static final String DISCLAIMER = "Alerta demonstrativo calculado pela API a partir das leituras registradas. "
            + "Nenhuma notificação (SMS, WhatsApp, e-mail ou push) é enviada. "
            + "Não substitui comunicados oficiais da Defesa Civil ou de órgãos de saúde.";

    private final HeatZoneService heatZoneService;
    private final TemperatureReadingRepository readingRepository;
    private final CoolingPointRepository coolingPointRepository;
    private final CoolingPointMapper coolingPointMapper;
    private final HeatAlertProperties properties;
    private final Clock clock;

    public HeatAlertResponse evaluate(Long heatZoneId) {
        HeatZone zone = heatZoneService.getEntity(heatZoneId);
        Instant now = clock.instant();
        Instant windowStart = now.minus(Duration.ofHours(properties.getLookbackHours()));

        List<TemperatureReading> readings = readingRepository.findBySensorHeatZoneIdAndMeasuredAtBetweenAndQualityNot(
                heatZoneId, windowStart, now, ReadingQuality.INVALID);

        HeatAlertResponse.HeatAlertResponseBuilder builder = HeatAlertResponse.builder()
                .heatZoneId(zone.getId())
                .heatZoneName(zone.getName())
                .registeredRiskLevel(zone.getRiskLevel())
                .evaluatedAt(now)
                .windowStart(windowStart)
                .windowEnd(now)
                .readingsConsidered(readings.size())
                .disclaimer(DISCLAIMER);

        if (readings.isEmpty()) {
            return builder
                    .alertLevel(AlertLevel.INSUFFICIENT_DATA)
                    .alertDescription(AlertLevel.INSUFFICIENT_DATA.getDescription())
                    .includesSimulatedData(false)
                    .recommendations(recommendationsFor(AlertLevel.INSUFFICIENT_DATA))
                    .coolingPoints(activeCoolingPoints(heatZoneId))
                    .build();
        }

        double maxHeatIndex = readings.stream()
                .mapToDouble(r -> HeatIndexCalculator.heatIndexCelsius(
                        r.getTemperatureCelsius().doubleValue(), r.getHumidityPercent().doubleValue()))
                .max()
                .orElseThrow();
        AlertLevel level = classify(BigDecimal.valueOf(maxHeatIndex));

        return builder
                .alertLevel(level)
                .alertDescription(level.getDescription())
                .averageTemperatureCelsius(round(readings.stream()
                        .mapToDouble(r -> r.getTemperatureCelsius().doubleValue()).average().orElseThrow()))
                .maxTemperatureCelsius(round(readings.stream()
                        .mapToDouble(r -> r.getTemperatureCelsius().doubleValue()).max().orElseThrow()))
                .averageHumidityPercent(round(readings.stream()
                        .mapToDouble(r -> r.getHumidityPercent().doubleValue()).average().orElseThrow()))
                .maxHeatIndexCelsius(round(maxHeatIndex))
                .latestReadingAt(readings.stream().map(TemperatureReading::getMeasuredAt)
                        .max(Comparator.naturalOrder()).orElseThrow())
                .includesSimulatedData(readings.stream().anyMatch(r -> r.getSource() == ReadingSource.SIMULATED))
                .recommendations(recommendationsFor(level))
                .coolingPoints(activeCoolingPoints(heatZoneId))
                .build();
    }

    AlertLevel classify(BigDecimal heatIndex) {
        if (heatIndex.compareTo(properties.getExtremeHeatIndex()) >= 0) {
            return AlertLevel.EXTREME;
        }
        if (heatIndex.compareTo(properties.getDangerHeatIndex()) >= 0) {
            return AlertLevel.DANGER;
        }
        if (heatIndex.compareTo(properties.getAlertHeatIndex()) >= 0) {
            return AlertLevel.ALERT;
        }
        if (heatIndex.compareTo(properties.getAttentionHeatIndex()) >= 0) {
            return AlertLevel.ATTENTION;
        }
        return AlertLevel.NORMAL;
    }

    private List<CoolingPointResponse> activeCoolingPoints(Long heatZoneId) {
        if (properties.getMaxCoolingPoints() == 0) {
            return List.of();
        }
        return coolingPointRepository.findByHeatZoneIdAndActiveTrue(heatZoneId,
                        PageRequest.of(0, properties.getMaxCoolingPoints(), Sort.by("capacity").descending()))
                .stream()
                .map(coolingPointMapper::toResponse)
                .toList();
    }

    private static List<String> recommendationsFor(AlertLevel level) {
        return switch (level) {
            case INSUFFICIENT_DATA -> List.of(
                    "Não há leituras válidas recentes para esta zona; verifique a comunicação dos sensores.",
                    "Acompanhe os comunicados oficiais de meteorologia e Defesa Civil.");
            case NORMAL -> List.of(
                    "Mantenha-se hidratado ao longo do dia.");
            case ATTENTION -> List.of(
                    "Beba água regularmente, mesmo sem sede.",
                    "Prefira roupas leves e de cores claras.",
                    "Evite exposição prolongada ao sol.");
            case ALERT -> List.of(
                    "Beba água regularmente e evite bebidas alcoólicas.",
                    "Evite atividades físicas intensas entre 10h e 16h.",
                    "Procure locais à sombra ou um ponto de resfriamento.",
                    "Observe crianças, idosos e pessoas com doenças crônicas.");
            case DANGER -> List.of(
                    "Evite exposição ao sol e atividades físicas ao ar livre.",
                    "Dirija-se a um ponto de resfriamento próximo.",
                    "Verifique o estado de idosos, crianças e pessoas vulneráveis.",
                    "Em caso de tontura, confusão ou mal-estar, procure atendimento de saúde.");
            case EXTREME -> List.of(
                    "Permaneça em local fresco e ventilado; procure um ponto de resfriamento.",
                    "Suspenda atividades ao ar livre.",
                    "Em caso de sintomas de insolação, ligue imediatamente para o SAMU (192).");
        };
    }

    private static BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }
}
