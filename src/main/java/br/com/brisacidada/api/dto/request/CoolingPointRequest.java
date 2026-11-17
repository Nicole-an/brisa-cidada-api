package br.com.brisacidada.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Dados para criar ou atualizar um ponto de resfriamento")
public class CoolingPointRequest {

    @Schema(example = "Biblioteca Municipal Climatizada", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 120, message = "deve ter no máximo 120 caracteres")
    private String name;

    @Schema(example = "Rua Exemplo, 100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 200, message = "deve ter no máximo 200 caracteres")
    private String address;

    @Schema(example = "Sé", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 120, message = "deve ter no máximo 120 caracteres")
    private String neighborhood;

    @Schema(example = "-23.551000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-90.0", message = "deve ser maior ou igual a -90")
    @DecimalMax(value = "90.0", message = "deve ser menor ou igual a 90")
    @Digits(integer = 3, fraction = 6, message = "deve ter no máximo 6 casas decimais")
    private BigDecimal latitude;

    @Schema(example = "-46.634000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-180.0", message = "deve ser maior ou igual a -180")
    @DecimalMax(value = "180.0", message = "deve ser menor ou igual a 180")
    @Digits(integer = 3, fraction = 6, message = "deve ter no máximo 6 casas decimais")
    private BigDecimal longitude;

    @Schema(example = "Seg-Sex 08:00-20:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 120, message = "deve ter no máximo 120 caracteres")
    private String openingHours;

    @Schema(example = "80", description = "Capacidade máxima de pessoas", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @Min(value = 1, message = "deve ser no mínimo 1")
    @Max(value = 100000, message = "deve ser no máximo 100000")
    private Integer capacity;

    @Schema(example = "Rampa de acesso, banheiro adaptado e bebedouro")
    @Size(max = 500, message = "deve ter no máximo 500 caracteres")
    private String accessibilityInfo;

    @Schema(example = "true", description = "Padrão: true")
    private Boolean active;

    @Schema(example = "1", description = "Zona de calor atendida (opcional)")
    @Positive(message = "deve ser positivo")
    private Long heatZoneId;
}
