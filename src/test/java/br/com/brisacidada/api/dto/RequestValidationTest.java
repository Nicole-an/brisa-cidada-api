package br.com.brisacidada.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.brisacidada.api.dto.request.CoolingPointRequest;
import br.com.brisacidada.api.dto.request.HeatZoneRequest;
import br.com.brisacidada.api.dto.request.SensorRequest;
import br.com.brisacidada.api.dto.request.TemperatureReadingRequest;
import br.com.brisacidada.api.entity.ReadingSource;
import br.com.brisacidada.api.entity.RiskLevel;
import br.com.brisacidada.api.entity.SensorStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validHeatZoneHasNoViolations() {
        assertThat(validator.validate(validZone())).isEmpty();
    }

    @Test
    void heatZoneRequiresFieldsAndValidCoordinates() {
        HeatZoneRequest request = validZone();
        request.setName(" ");
        request.setLatitude(new BigDecimal("91"));
        request.setRiskLevel(null);

        assertThat(fields(validator.validate(request))).containsExactlyInAnyOrder("name", "latitude", "riskLevel");
    }

    @Test
    void sensorRejectsInvalidCodeAndFutureInstallation() {
        SensorRequest request = new SensorRequest();
        request.setCode("código inválido!");
        request.setName("Sensor");
        request.setLatitude(BigDecimal.ZERO);
        request.setLongitude(BigDecimal.ZERO);
        request.setStatus(SensorStatus.ACTIVE);
        request.setInstallationDate(LocalDate.now().plusDays(2));
        request.setHeatZoneId(0L);

        assertThat(fields(validator.validate(request)))
                .containsExactlyInAnyOrder("code", "installationDate", "heatZoneId");
    }

    @Test
    void readingRejectsOutOfRangeValuesAndFutureInstant() {
        TemperatureReadingRequest request = new TemperatureReadingRequest();
        request.setSensorId(1L);
        request.setMeasuredAt(Instant.now().plusSeconds(3600));
        request.setTemperatureCelsius(new BigDecimal("80"));
        request.setHumidityPercent(new BigDecimal("101"));
        request.setSource(ReadingSource.SENSOR);

        assertThat(fields(validator.validate(request)))
                .containsExactlyInAnyOrder("measuredAt", "temperatureCelsius", "humidityPercent");
    }

    @Test
    void coolingPointRequiresPositiveCapacityAndLimitsText() {
        CoolingPointRequest request = new CoolingPointRequest();
        request.setName("Ponto");
        request.setAddress("Rua A");
        request.setNeighborhood("Centro");
        request.setLatitude(BigDecimal.ZERO);
        request.setLongitude(BigDecimal.ZERO);
        request.setOpeningHours("24h");
        request.setCapacity(0);
        request.setAccessibilityInfo("x".repeat(501));

        assertThat(fields(validator.validate(request))).containsExactlyInAnyOrder("capacity", "accessibilityInfo");
    }

    private static HeatZoneRequest validZone() {
        HeatZoneRequest request = new HeatZoneRequest();
        request.setName("Zona");
        request.setNeighborhood("Centro");
        request.setCity("São Paulo");
        request.setLatitude(new BigDecimal("-23.550520"));
        request.setLongitude(new BigDecimal("-46.633308"));
        request.setRiskLevel(RiskLevel.MODERATE);
        return request;
    }

    private static <T> Set<String> fields(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }
}
