package br.com.brisacidada.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.brisacidada.api.dto.request.HeatZoneRequest;
import br.com.brisacidada.api.dto.request.SensorRequest;
import br.com.brisacidada.api.dto.request.TemperatureReadingRequest;
import br.com.brisacidada.api.entity.HeatZone;
import br.com.brisacidada.api.entity.ReadingSource;
import br.com.brisacidada.api.entity.RiskLevel;
import br.com.brisacidada.api.entity.Sensor;
import br.com.brisacidada.api.entity.SensorStatus;
import br.com.brisacidada.api.exception.ConflictException;
import br.com.brisacidada.api.repository.HeatZoneRepository;
import br.com.brisacidada.api.repository.SensorRepository;
import br.com.brisacidada.api.repository.TemperatureReadingRepository;
import br.com.brisacidada.api.service.HeatZoneService;
import br.com.brisacidada.api.service.SensorService;
import br.com.brisacidada.api.service.TemperatureReadingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class SqlServerContainerTest {

    @Container
    static final MSSQLServerContainer SQL_SERVER =
            new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQL_SERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQL_SERVER::getUsername);
        registry.add("spring.datasource.password", SQL_SERVER::getPassword);
    }

    @Autowired
    private HeatZoneService heatZoneService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private TemperatureReadingService readingService;
    @Autowired
    private HeatZoneRepository heatZoneRepository;
    @Autowired
    private SensorRepository sensorRepository;
    @Autowired
    private TemperatureReadingRepository readingRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void persistsEntitiesAndRelationshipsOnSqlServer() {
        HeatZoneRequest zoneRequest = new HeatZoneRequest();
        zoneRequest.setName("Zona SQL Server");
        zoneRequest.setNeighborhood("Centro");
        zoneRequest.setCity("São Paulo");
        zoneRequest.setLatitude(new BigDecimal("-23.550520"));
        zoneRequest.setLongitude(new BigDecimal("-46.633308"));
        zoneRequest.setRiskLevel(RiskLevel.EXTREME);
        Long zoneId = heatZoneService.create(zoneRequest).getId();

        SensorRequest sensorRequest = new SensorRequest();
        sensorRequest.setCode("MSSQL-001");
        sensorRequest.setName("Sensor SQL Server");
        sensorRequest.setLatitude(new BigDecimal("-23.5506"));
        sensorRequest.setLongitude(new BigDecimal("-46.6334"));
        sensorRequest.setStatus(SensorStatus.ACTIVE);
        sensorRequest.setInstallationDate(LocalDate.of(2025, 3, 10));
        sensorRequest.setHeatZoneId(zoneId);
        Long sensorId = sensorService.create(sensorRequest).getId();

        Instant measuredAt = Instant.now().minus(5, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS);
        TemperatureReadingRequest readingRequest = new TemperatureReadingRequest();
        readingRequest.setSensorId(sensorId);
        readingRequest.setMeasuredAt(measuredAt);
        readingRequest.setTemperatureCelsius(new BigDecimal("36.40"));
        readingRequest.setHumidityPercent(new BigDecimal("48.50"));
        readingRequest.setSource(ReadingSource.SENSOR);
        Long readingId = readingService.create(readingRequest).getId();

        transactionTemplate.executeWithoutResult(status -> {
            Sensor sensor = sensorRepository.findById(sensorId).orElseThrow();
            assertThat(sensor.getHeatZone().getName()).isEqualTo("Zona SQL Server");
            assertThat(sensor.getLastCommunicationAt()).isEqualTo(measuredAt);
            var reading = readingRepository.findById(readingId).orElseThrow();
            assertThat(reading.getTemperatureCelsius()).isEqualByComparingTo("36.40");
            assertThat(reading.getSensor().getId()).isEqualTo(sensorId);
        });

        assertThat(readingService.findById(readingId).getMeasuredAt()).isEqualTo(measuredAt);
        assertThatThrownBy(() -> heatZoneService.delete(zoneId)).isInstanceOf(ConflictException.class);
    }

    @Test
    void databaseEnforcesUniqueConstraint() {
        HeatZoneRequest zoneRequest = new HeatZoneRequest();
        zoneRequest.setName("Zona restrição");
        zoneRequest.setNeighborhood("Centro");
        zoneRequest.setCity("Rio de Janeiro");
        zoneRequest.setLatitude(new BigDecimal("-22.9"));
        zoneRequest.setLongitude(new BigDecimal("-43.2"));
        zoneRequest.setRiskLevel(RiskLevel.LOW);
        heatZoneService.create(zoneRequest);

        // Ignora a verificação do service para garantir que o UNIQUE do banco também protege os dados.
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            HeatZone duplicate = new HeatZone();
            duplicate.setName("Zona restrição");
            duplicate.setNeighborhood("Outro");
            duplicate.setCity("Rio de Janeiro");
            duplicate.setLatitude(new BigDecimal("-22.9"));
            duplicate.setLongitude(new BigDecimal("-43.2"));
            duplicate.setRiskLevel(RiskLevel.LOW);
            heatZoneRepository.saveAndFlush(duplicate);
        })).isInstanceOf(DataIntegrityViolationException.class);
    }
}
