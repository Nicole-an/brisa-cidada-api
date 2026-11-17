package br.com.brisacidada.api.controller;

import br.com.brisacidada.api.dto.request.CoolingPointRequest;
import br.com.brisacidada.api.dto.response.CoolingPointResponse;
import br.com.brisacidada.api.dto.response.ErrorResponse;
import br.com.brisacidada.api.dto.response.PageResponse;
import br.com.brisacidada.api.service.CoolingPointService;
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
@RequestMapping("/api/v1/cooling-points")
@RequiredArgsConstructor
@Tag(name = "Pontos de resfriamento", description = "Locais públicos climatizados ou sombreados para refúgio do calor")
public class CoolingPointController {

    private final CoolingPointService coolingPointService;

    @PostMapping
    @Operation(summary = "Cadastra um ponto de resfriamento")
    @ApiResponse(responseCode = "201", description = "Ponto criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Zona de calor informada não existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Ponto com mesmo nome e endereço já existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<CoolingPointResponse> create(@Valid @RequestBody CoolingPointRequest request) {
        CoolingPointResponse created = coolingPointService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista pontos de resfriamento (paginado), opcionalmente por zona")
    @ApiResponse(responseCode = "200", description = "Página de pontos")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<CoolingPointResponse> findAll(
            @Parameter(description = "Filtra pela zona de calor") @RequestParam(required = false) Long heatZoneId,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return coolingPointService.findAll(heatZoneId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um ponto de resfriamento")
    @ApiResponse(responseCode = "200", description = "Ponto encontrado")
    @ApiResponse(responseCode = "404", description = "Ponto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CoolingPointResponse findById(@PathVariable Long id) {
        return coolingPointService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um ponto de resfriamento")
    @ApiResponse(responseCode = "200", description = "Ponto atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Ponto ou zona de calor não encontrados",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Ponto com mesmo nome e endereço já existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CoolingPointResponse update(@PathVariable Long id, @Valid @RequestBody CoolingPointRequest request) {
        return coolingPointService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um ponto de resfriamento")
    @ApiResponse(responseCode = "204", description = "Ponto excluído")
    @ApiResponse(responseCode = "404", description = "Ponto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coolingPointService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
