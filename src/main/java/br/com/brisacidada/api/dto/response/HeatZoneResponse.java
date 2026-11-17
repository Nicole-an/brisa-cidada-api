package br.com.brisacidada.api.dto.response;

import br.com.brisacidada.api.entity.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Zona de calor monitorada")
public class HeatZoneResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "Praça da Sé e entorno")
    private String name;
    @Schema(example = "Sé")
    private String neighborhood;
    @Schema(example = "São Paulo")
    private String city;
    @Schema(example = "-23.550520")
    private BigDecimal latitude;
    @Schema(example = "-46.633308")
    private BigDecimal longitude;
    @Schema(example = "HIGH")
    private RiskLevel riskLevel;
    @Schema(example = "true")
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
