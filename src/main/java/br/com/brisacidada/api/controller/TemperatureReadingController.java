package br.com.brisacidada.api.controller;

import br.com.brisacidada.api.dto.request.TemperatureReadingRequest;
import br.com.brisacidada.api.dto.response.ErrorResponse;
import br.com.brisacidada.api.dto.response.PageResponse;
import br.com.brisacidada.api.dto.response.TemperatureReadingResponse;
import br.com.brisacidada.api.service.TemperatureReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/temperature-readings")
@RequiredArgsConstructor
@Tag(name = "Leituras de temperatura", description = "Medições de temperatura e umidade dos sensores")
public class TemperatureReadingController {

    private final TemperatureReadingService readingService;

    @PostMapping
    @Operation(summary = "Registra uma leitura",
            description = "Atualiza a última comunicação do sensor quando a leitura é a mais recente.")
    @ApiResponse(responseCode = "201", description = "Leitura registrada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Sensor informado não existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Já existe leitura do sensor no mesmo instante",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TemperatureReadingResponse> create(@Valid @RequestBody TemperatureReadingRequest request) {
        TemperatureReadingResponse created = readingService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista leituras (paginado, mais recentes primeiro), opcionalmente por sensor")
    @ApiResponse(responseCode = "200", description = "Página de leituras")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<TemperatureReadingResponse> findAll(
            @Parameter(description = "Filtra pelo sensor") @RequestParam(required = false) Long sensorId,
            @ParameterObject @PageableDefault(sort = "measuredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return readingService.findAll(sensorId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma leitura")
    @ApiResponse(responseCode = "200", description = "Leitura encontrada")
    @ApiResponse(responseCode = "404", description = "Leitura não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TemperatureReadingResponse findById(@PathVariable Long id) {
        return readingService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma leitura")
    @ApiResponse(responseCode = "200", description = "Leitura atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Leitura ou sensor não encontrados",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Já existe leitura do sensor no mesmo instante",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TemperatureReadingResponse update(@PathVariable Long id,
                                             @Valid @RequestBody TemperatureReadingRequest request) {
        return readingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma leitura")
    @ApiResponse(responseCode = "204", description = "Leitura excluída")
    @ApiResponse(responseCode = "404", description = "Leitura não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        readingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
