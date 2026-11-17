package br.com.brisacidada.api.dto.response;

import br.com.brisacidada.api.entity.SensorStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Sensor de temperatura e umidade")
public class SensorResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "SP-SE-001")
    private String code;
    @Schema(example = "Sensor Praça da Sé")
    private String name;
    @Schema(example = "-23.550600")
    private BigDecimal latitude;
    @Schema(example = "-46.633400")
    private BigDecimal longitude;
    @Schema(example = "ACTIVE")
    private SensorStatus status;
    @Schema(example = "2025-03-10")
    private LocalDate installationDate;
    @Schema(description = "Instante da leitura mais recente recebida")
    private Instant lastCommunicationAt;
    @Schema(example = "1")
    private Long heatZoneId;
    @Schema(example = "Praça da Sé e entorno")
    private String heatZoneName;
    private Instant createdAt;
    private Instant updatedAt;
}
