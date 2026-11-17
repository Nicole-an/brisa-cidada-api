package br.com.brisacidada.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Ponto público de resfriamento")
public class CoolingPointResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "Biblioteca Municipal Climatizada")
    private String name;
    @Schema(example = "Rua Exemplo, 100")
    private String address;
    @Schema(example = "Sé")
    private String neighborhood;
    @Schema(example = "-23.551000")
    private BigDecimal latitude;
    @Schema(example = "-46.634000")
    private BigDecimal longitude;
    @Schema(example = "Seg-Sex 08:00-20:00")
    private String openingHours;
    @Schema(example = "80")
    private Integer capacity;
    @Schema(example = "Rampa de acesso, banheiro adaptado e bebedouro")
    private String accessibilityInfo;
    @Schema(example = "true")
    private Boolean active;
    @Schema(example = "1", nullable = true)
    private Long heatZoneId;
    @Schema(example = "Praça da Sé e entorno", nullable = true)
    private String heatZoneName;
    private Instant createdAt;
    private Instant updatedAt;
}
