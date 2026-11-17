package br.com.brisacidada.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Resposta padronizada de erro")
public record ErrorResponse(
        @Schema(example = "2026-01-15T14:30:00Z") Instant timestamp,
        @Schema(example = "404") int status,
        @Schema(example = "Not Found") String error,
        @Schema(example = "Zona de calor 99 não encontrada.") String message,
        @Schema(example = "/api/v1/heat-zones/99") String path,
        List<FieldError> fieldErrors) {

    @Schema(description = "Erro de validação de um campo")
    public record FieldError(
            @Schema(example = "name") String field,
            @Schema(example = "é obrigatório") String message) {
    }
}
