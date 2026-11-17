package br.com.brisacidada.api.dto.request;

import br.com.brisacidada.api.entity.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Dados para criar ou atualizar uma zona de calor")
public class HeatZoneRequest {

    @Schema(example = "Praça da Sé e entorno", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 120, message = "deve ter no máximo 120 caracteres")
    private String name;

    @Schema(example = "Sé", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 120, message = "deve ter no máximo 120 caracteres")
    private String neighborhood;

    @Schema(example = "São Paulo", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 120, message = "deve ter no máximo 120 caracteres")
    private String city;

    @Schema(example = "-23.550520", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-90.0", message = "deve ser maior ou igual a -90")
    @DecimalMax(value = "90.0", message = "deve ser menor ou igual a 90")
    @Digits(integer = 3, fraction = 6, message = "deve ter no máximo 6 casas decimais")
    private BigDecimal latitude;

    @Schema(example = "-46.633308", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-180.0", message = "deve ser maior ou igual a -180")
    @DecimalMax(value = "180.0", message = "deve ser menor ou igual a 180")
    @Digits(integer = 3, fraction = 6, message = "deve ter no máximo 6 casas decimais")
    private BigDecimal longitude;

    @Schema(example = "HIGH", description = "Classificação de risco cadastrada para a zona",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    private RiskLevel riskLevel;

    @Schema(example = "true", description = "Padrão: true")
    private Boolean active;
}
