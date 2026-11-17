package br.com.brisacidada.api;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullCrudFlowAcrossAllResources() throws Exception {
        String suffix = uniqueSuffix();

        long zoneId = idOf(postJson("/api/v1/heat-zones", zoneJson("Zona " + suffix, "HIGH"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt", notNullValue())));

        mockMvc.perform(get("/api/v1/heat-zones/{id}", zoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zona " + suffix));

        mockMvc.perform(put("/api/v1/heat-zones/{id}", zoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zoneJson("Zona " + suffix, "EXTREME")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("EXTREME"));

        long sensorId = idOf(postJson("/api/v1/sensors", sensorJson("S-" + suffix, zoneId))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.heatZoneId").value(zoneId))
                .andExpect(jsonPath("$.heatZoneName").value("Zona " + suffix))
                .andExpect(jsonPath("$.lastCommunicationAt", nullValue())));

        String measuredAt = Instant.now().minus(10, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS).toString();
        long readingId = idOf(postJson("/api/v1/temperature-readings", readingJson(sensorId, measuredAt, "35.00", "60.00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sensorId").value(sensorId))
                .andExpect(jsonPath("$.sensorCode").value("S-" + suffix))
                .andExpect(jsonPath("$.quality").value("VALID")));

        mockMvc.perform(get("/api/v1/sensors/{id}", sensorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastCommunicationAt").value(measuredAt));

        long pointId = idOf(postJson("/api/v1/cooling-points", coolingPointJson("Ponto " + suffix, zoneId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.heatZoneId").value(zoneId)));

        mockMvc.perform(get("/api/v1/heat-zones/{id}/heat-alert", zoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertLevel").value("DANGER"))
                .andExpect(jsonPath("$.readingsConsidered").value(1))
                .andExpect(jsonPath("$.includesSimulatedData").value(true))
                .andExpect(jsonPath("$.coolingPoints", hasSize(1)))
                .andExpect(jsonPath("$.disclaimer", notNullValue()));

        mockMvc.perform(get("/api/v1/temperature-readings").param("sensorId", String.valueOf(sensorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(readingId));

        mockMvc.perform(delete("/api/v1/heat-zones/{id}", zoneId)).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/v1/sensors/{id}", sensorId)).andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/temperature-readings/{id}", readingId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/sensors/{id}", sensorId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/cooling-points/{id}", pointId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/heat-zones/{id}", zoneId)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/heat-zones/{id}", zoneId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/heat-zones/" + zoneId));
    }

    @Test
    void createReturnsLocationHeaderPointingToResource() throws Exception {
        ResultActions result = postJson("/api/v1/heat-zones", zoneJson("Local " + uniqueSuffix(), "LOW"))
                .andExpect(status().isCreated());
        long id = idOf(result);
        result.andExpect(header().string("Location", endsWith("/api/v1/heat-zones/" + id)));
    }

    @Test
    void invalidPayloadReturns400WithFieldErrors() throws Exception {
        postJson("/api/v1/heat-zones", """
                {"name": "", "neighborhood": "Centro", "city": "São Paulo", "latitude": 120, "longitude": 0}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'name')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'latitude')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'riskLevel')]").exists());
    }

    @Test
    void invalidEnumAndMalformedJsonReturn400WithoutInternalDetails() throws Exception {
        postJson("/api/v1/heat-zones", zoneJson("Enum " + uniqueSuffix(), "SUPER_HOT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());

        postJson("/api/v1/heat-zones", "{ not-json")
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidPathAndSortParametersReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/sensors/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/heat-zones").param("sort", "doesNotExist"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void referencingMissingResourcesReturns404() throws Exception {
        postJson("/api/v1/sensors", sensorJson("MISSING-" + uniqueSuffix(), 999_999L))
                .andExpect(status().isNotFound());
        postJson("/api/v1/temperature-readings",
                readingJson(999_999L, Instant.now().minusSeconds(60).truncatedTo(ChronoUnit.SECONDS).toString(), "30", "50"))
                .andExpect(status().isNotFound());
        postJson("/api/v1/cooling-points", coolingPointJson("Sem zona " + uniqueSuffix(), 999_999L))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/cooling-points/{id}", 999_999L)).andExpect(status().isNotFound());
    }

    @Test
    void duplicatesReturn409() throws Exception {
        String suffix = uniqueSuffix();
        long zoneId = idOf(postJson("/api/v1/heat-zones", zoneJson("Dup " + suffix, "LOW")));
        postJson("/api/v1/heat-zones", zoneJson("Dup " + suffix, "LOW")).andExpect(status().isConflict());

        long sensorId = idOf(postJson("/api/v1/sensors", sensorJson("DUP-" + suffix, zoneId)));
        postJson("/api/v1/sensors", sensorJson("dup-" + suffix, zoneId)).andExpect(status().isConflict());

        String measuredAt = Instant.now().minusSeconds(120).truncatedTo(ChronoUnit.SECONDS).toString();
        postJson("/api/v1/temperature-readings", readingJson(sensorId, measuredAt, "30", "50"))
                .andExpect(status().isCreated());
        postJson("/api/v1/temperature-readings", readingJson(sensorId, measuredAt, "31", "50"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void coolingPointWithoutZoneIsAllowed() throws Exception {
        postJson("/api/v1/cooling-points", coolingPointJson("Avulso " + uniqueSuffix(), null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.heatZoneId", nullValue()))
                .andExpect(jsonPath("$.heatZoneName", nullValue()));
    }

    @Test
    void heatAlertWithoutReadingsReportsInsufficientData() throws Exception {
        long zoneId = idOf(postJson("/api/v1/heat-zones", zoneJson("Sem leituras " + uniqueSuffix(), "MODERATE")));
        mockMvc.perform(get("/api/v1/heat-zones/{id}/heat-alert", zoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertLevel").value("INSUFFICIENT_DATA"))
                .andExpect(jsonPath("$.readingsConsidered").value(0));
        mockMvc.perform(get("/api/v1/heat-zones/{id}/heat-alert", 999_999L)).andExpect(status().isNotFound());
    }

    @Test
    void rootRedirectsToSwaggerAndDocsAndHealthAreAvailable() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui.html"));
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/heat-zones']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/sensors/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/temperature-readings']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/cooling-points']").exists());
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void unknownRouteReturns404AndWrongMethodReturns405() throws Exception {
        mockMvc.perform(get("/api/v1/unknown")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/heat-zones/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static long idOf(ResultActions result) throws Exception {
        Number id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String zoneJson(String name, String riskLevel) {
        return """
                {"name": "%s", "neighborhood": "Centro", "city": "São Paulo",
                 "latitude": -23.550520, "longitude": -46.633308, "riskLevel": "%s"}
                """.formatted(name, riskLevel);
    }

    private static String sensorJson(String code, long zoneId) {
        return """
                {"code": "%s", "name": "Sensor teste", "latitude": -23.5506, "longitude": -46.6334,
                 "status": "ACTIVE", "installationDate": "2025-03-10", "heatZoneId": %d}
                """.formatted(code, zoneId);
    }

    private static String readingJson(long sensorId, String measuredAt, String temperature, String humidity) {
        return """
                {"sensorId": %d, "measuredAt": "%s", "temperatureCelsius": %s, "humidityPercent": %s,
                 "source": "SIMULATED"}
                """.formatted(sensorId, measuredAt, temperature, humidity);
    }

    private static String coolingPointJson(String name, Long zoneId) {
        return """
                {"name": "%s", "address": "Rua Teste, 10", "neighborhood": "Centro",
                 "latitude": -23.551, "longitude": -46.634, "openingHours": "Seg-Sex 08:00-18:00",
                 "capacity": 50, "accessibilityInfo": "Rampa de acesso", "heatZoneId": %s}
                """.formatted(name, zoneId == null ? "null" : zoneId.toString());
    }
}
