package br.com.brisacidada.api.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.brisacidada.api.dto.response.HeatZoneResponse;
import br.com.brisacidada.api.entity.RiskLevel;
import br.com.brisacidada.api.exception.ConflictException;
import br.com.brisacidada.api.exception.ResourceNotFoundException;
import br.com.brisacidada.api.service.HeatAlertService;
import br.com.brisacidada.api.service.HeatZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HeatZoneController.class)
class HeatZoneControllerTest {

    private static final String VALID_BODY = """
            {"name": "Zona", "neighborhood": "Centro", "city": "SP",
             "latitude": -23.55, "longitude": -46.63, "riskLevel": "HIGH"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HeatZoneService heatZoneService;

    @MockitoBean
    private HeatAlertService heatAlertService;

    @Test
    void createReturns201WithLocation() throws Exception {
        when(heatZoneService.create(any())).thenReturn(response(15L));

        mockMvc.perform(post("/api/v1/heat-zones").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/heat-zones/15")))
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"));
    }

    @Test
    void createWithInvalidBodyReturns400AndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/api/v1/heat-zones").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos."))
                .andExpect(jsonPath("$.fieldErrors").isArray());
        verifyNoInteractions(heatZoneService);
    }

    @Test
    void getMissingReturns404() throws Exception {
        when(heatZoneService.findById(9L)).thenThrow(new ResourceNotFoundException("Zona de calor", 9L));

        mockMvc.perform(get("/api/v1/heat-zones/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void updateConflictReturns409() throws Exception {
        when(heatZoneService.update(eq(1L), any())).thenThrow(new ConflictException("duplicada"));

        mockMvc.perform(put("/api/v1/heat-zones/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("duplicada"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/heat-zones/1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteWithDependentsReturns409() throws Exception {
        doThrow(new ConflictException("vinculada")).when(heatZoneService).delete(2L);

        mockMvc.perform(delete("/api/v1/heat-zones/2")).andExpect(status().isConflict());
    }

    @Test
    void unexpectedErrorReturnsGeneric500WithoutDetails() throws Exception {
        when(heatZoneService.findById(3L)).thenThrow(new IllegalStateException("segredo interno"));

        mockMvc.perform(get("/api/v1/heat-zones/3"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno. Tente novamente mais tarde."))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    private static HeatZoneResponse response(Long id) {
        HeatZoneResponse response = new HeatZoneResponse();
        response.setId(id);
        response.setName("Zona");
        response.setRiskLevel(RiskLevel.HIGH);
        response.setActive(true);
        return response;
    }
}
