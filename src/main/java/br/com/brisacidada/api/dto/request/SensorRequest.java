package br.com.brisacidada.api.dto.request;

import br.com.brisacidada.api.entity.SensorStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Dados para criar ou atualizar um sensor")
public class SensorRequest {

    @Schema(example = "SP-SE-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 40, message = "deve ter no máximo 40 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "deve conter apenas letras, números e hífen")
    private String code;

    @Schema(example = "Sensor Praça da Sé", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "é obrigatório")
    @Size(max = 120, message = "deve ter no máximo 120 caracteres")
    private String name;

    @Schema(example = "-23.550600", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-90.0", message = "deve ser maior ou igual a -90")
    @DecimalMax(value = "90.0", message = "deve ser menor ou igual a 90")
    @Digits(integer = 3, fraction = 6, message = "deve ter no máximo 6 casas decimais")
    private BigDecimal latitude;

    @Schema(example = "-46.633400", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-180.0", message = "deve ser maior ou igual a -180")
    @DecimalMax(value = "180.0", message = "deve ser menor ou igual a 180")
    @Digits(integer = 3, fraction = 6, message = "deve ter no máximo 6 casas decimais")
    private BigDecimal longitude;

    @Schema(example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    private SensorStatus status;

    @Schema(example = "2025-03-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @PastOrPresent(message = "não pode estar no futuro")
    private LocalDate installationDate;

    @Schema(example = "1", description = "Zona de calor onde o sensor está instalado",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "é obrigatório")
    @Positive(message = "deve ser positivo")
    private Long heatZoneId;
}
