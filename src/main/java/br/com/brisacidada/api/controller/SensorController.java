package br.com.brisacidada.api.controller;

import br.com.brisacidada.api.dto.request.SensorRequest;
import br.com.brisacidada.api.dto.response.ErrorResponse;
import br.com.brisacidada.api.dto.response.PageResponse;
import br.com.brisacidada.api.dto.response.SensorResponse;
import br.com.brisacidada.api.service.SensorService;
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
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
@Tag(name = "Sensores", description = "Sensores de temperatura e umidade instalados nas zonas de calor")
public class SensorController {

    private final SensorService sensorService;

    @PostMapping
    @Operation(summary = "Cadastra um sensor")
    @ApiResponse(responseCode = "201", description = "Sensor criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Zona de calor informada não existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Código de sensor já cadastrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SensorResponse> create(@Valid @RequestBody SensorRequest request) {
        SensorResponse created = sensorService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista sensores (paginado), opcionalmente por zona")
    @ApiResponse(responseCode = "200", description = "Página de sensores")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<SensorResponse> findAll(
            @Parameter(description = "Filtra pela zona de calor") @RequestParam(required = false) Long heatZoneId,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return sensorService.findAll(heatZoneId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um sensor")
    @ApiResponse(responseCode = "200", description = "Sensor encontrado")
    @ApiResponse(responseCode = "404", description = "Sensor não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public SensorResponse findById(@PathVariable Long id) {
        return sensorService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um sensor")
    @ApiResponse(responseCode = "200", description = "Sensor atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Sensor ou zona de calor não encontrados",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Código de sensor já cadastrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public SensorResponse update(@PathVariable Long id, @Valid @RequestBody SensorRequest request) {
        return sensorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um sensor")
    @ApiResponse(responseCode = "204", description = "Sensor excluído")
    @ApiResponse(responseCode = "404", description = "Sensor não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Sensor possui leituras registradas",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sensorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
