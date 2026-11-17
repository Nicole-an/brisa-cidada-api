package br.com.brisacidada.api.controller;

import br.com.brisacidada.api.dto.request.HeatZoneRequest;
import br.com.brisacidada.api.dto.response.ErrorResponse;
import br.com.brisacidada.api.dto.response.HeatAlertResponse;
import br.com.brisacidada.api.dto.response.HeatZoneResponse;
import br.com.brisacidada.api.dto.response.PageResponse;
import br.com.brisacidada.api.service.HeatAlertService;
import br.com.brisacidada.api.service.HeatZoneService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/heat-zones")
@RequiredArgsConstructor
@Tag(name = "Zonas de calor", description = "Áreas urbanas monitoradas quanto à exposição ao calor")
public class HeatZoneController {

    private final HeatZoneService heatZoneService;
    private final HeatAlertService heatAlertService;

    @PostMapping
    @Operation(summary = "Cria uma zona de calor")
    @ApiResponse(responseCode = "201", description = "Zona criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Zona com mesmo nome já existe na cidade",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<HeatZoneResponse> create(@Valid @RequestBody HeatZoneRequest request) {
        HeatZoneResponse created = heatZoneService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista zonas de calor (paginado)")
    @ApiResponse(responseCode = "200", description = "Página de zonas")
    @ApiResponse(responseCode = "400", description = "Parâmetros de paginação/ordenação inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<HeatZoneResponse> findAll(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return heatZoneService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma zona de calor")
    @ApiResponse(responseCode = "200", description = "Zona encontrada")
    @ApiResponse(responseCode = "404", description = "Zona não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public HeatZoneResponse findById(@PathVariable Long id) {
        return heatZoneService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma zona de calor")
    @ApiResponse(responseCode = "200", description = "Zona atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Zona não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Zona com mesmo nome já existe na cidade",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public HeatZoneResponse update(@PathVariable Long id, @Valid @RequestBody HeatZoneRequest request) {
        return heatZoneService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma zona de calor")
    @ApiResponse(responseCode = "204", description = "Zona excluída")
    @ApiResponse(responseCode = "404", description = "Zona não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Zona possui sensores ou pontos de resfriamento vinculados",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        heatZoneService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/heat-alert")
    @Operation(summary = "Calcula o alerta demonstrativo de calor da zona",
            description = "Usa as leituras válidas/suspeitas da janela configurada (brisa.heat-alert.lookback-hours), "
                    + "calcula o índice de calor e classifica o nível por limiares configuráveis. "
                    + "Apenas retorna o alerta; nenhuma notificação externa é enviada.")
    @ApiResponse(responseCode = "200", description = "Alerta calculado")
    @ApiResponse(responseCode = "404", description = "Zona não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public HeatAlertResponse heatAlert(@PathVariable Long id) {
        return heatAlertService.evaluate(id);
    }
}
