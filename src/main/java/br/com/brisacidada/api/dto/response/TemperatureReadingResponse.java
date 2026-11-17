package br.com.brisacidada.api.dto.response;

import br.com.brisacidada.api.entity.ReadingQuality;
import br.com.brisacidada.api.entity.ReadingSource;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Leitura de temperatura e umidade")
public class TemperatureReadingResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long sensorId;
    @Schema(example = "SP-SE-001")
    private String sensorCode;
    @Schema(example = "2026-01-15T14:30:00Z")
    private Instant measuredAt;
    @Schema(example = "34.50")
    private BigDecimal temperatureCelsius;
    @Schema(example = "55.00")
    private BigDecimal humidityPercent;
    @Schema(example = "SENSOR")
    private ReadingSource source;
    @Schema(example = "VALID")
    private ReadingQuality quality;
    private Instant createdAt;
    private Instant updatedAt;
}
