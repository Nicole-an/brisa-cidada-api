package br.com.brisacidada.api.dto.request;

import br.com.brisacidada.api.entity.ReadingQuality;
import br.com.brisacidada.api.entity.ReadingSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Dados para registrar ou atualizar uma leitura de temperatura")
public class TemperatureReadingRequest {

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @Positive(message = "deve ser positivo")
    private Long sensorId;

    @Schema(example = "2026-01-15T14:30:00Z", description = "Instante da medição (ISO-8601, UTC)",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @PastOrPresent(message = "não pode estar no futuro")
    private Instant measuredAt;

    @Schema(example = "34.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-50.0", message = "deve ser maior ou igual a -50")
    @DecimalMax(value = "70.0", message = "deve ser menor ou igual a 70")
    @Digits(integer = 2, fraction = 2, message = "deve ter no máximo 2 casas decimais")
    private BigDecimal temperatureCelsius;

    @Schema(example = "55.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "0.0", message = "deve ser maior ou igual a 0")
    @DecimalMax(value = "100.0", message = "deve ser menor ou igual a 100")
    @Digits(integer = 3, fraction = 2, message = "deve ter no máximo 2 casas decimais")
    private BigDecimal humidityPercent;

    @Schema(example = "SENSOR", description = "Use SIMULATED para dados demonstrativos",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    private ReadingSource source;

    @Schema(example = "VALID", description = "Padrão: VALID")
    private ReadingQuality quality;
}
