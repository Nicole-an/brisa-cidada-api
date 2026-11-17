package br.com.brisacidada.api.dto.response;

import br.com.brisacidada.api.entity.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Alerta demonstrativo de calor calculado pela API para uma zona. Nenhuma notificação externa é enviada.")
public record HeatAlertResponse(
        @Schema(example = "1") Long heatZoneId,
        @Schema(example = "Praça da Sé e entorno") String heatZoneName,
        @Schema(example = "HIGH", description = "Risco cadastrado na zona") RiskLevel registeredRiskLevel,
        @Schema(example = "DANGER") AlertLevel alertLevel,
        @Schema(example = "Perigo: exaustão provável e risco de insolação") String alertDescription,
        Instant evaluatedAt,
        Instant windowStart,
        Instant windowEnd,
        @Schema(example = "4") int readingsConsidered,
        @Schema(example = "34.8") BigDecimal averageTemperatureCelsius,
        @Schema(example = "36.2") BigDecimal maxTemperatureCelsius,
        @Schema(example = "52.5") BigDecimal averageHumidityPercent,
        @Schema(example = "42.3", description = "Maior índice de calor (sensação térmica) entre as leituras")
        BigDecimal maxHeatIndexCelsius,
        Instant latestReadingAt,
        @Schema(description = "Indica se alguma leitura considerada tem origem SIMULATED")
        boolean includesSimulatedData,
        List<String> recommendations,
        List<CoolingPointResponse> coolingPoints,
        String disclaimer) {
}
