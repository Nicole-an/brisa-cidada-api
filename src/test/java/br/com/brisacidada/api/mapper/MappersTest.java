package br.com.brisacidada.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.brisacidada.api.config.ModelMapperConfig;
import br.com.brisacidada.api.dto.request.CoolingPointRequest;
import br.com.brisacidada.api.dto.request.SensorRequest;
import br.com.brisacidada.api.dto.request.TemperatureReadingRequest;
import br.com.brisacidada.api.entity.CoolingPoint;
import br.com.brisacidada.api.entity.HeatZone;
import br.com.brisacidada.api.entity.ReadingSource;
import br.com.brisacidada.api.entity.Sensor;
import br.com.brisacidada.api.entity.SensorStatus;
import br.com.brisacidada.api.entity.TemperatureReading;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

class MappersTest {

    private final ModelMapper modelMapper = new ModelMapperConfig().modelMapper();
    private final SensorMapper sensorMapper = new SensorMapper(modelMapper);
    private final TemperatureReadingMapper readingMapper = new TemperatureReadingMapper(modelMapper);
    private final CoolingPointMapper coolingPointMapper = new CoolingPointMapper(modelMapper);

    @Test
    void sensorRequestDoesNotCreateOrChangeRelatedZone() {
        SensorRequest request = new SensorRequest();
        request.setCode("SP-1");
        request.setName("Sensor");
        request.setLatitude(new BigDecimal("-23.5"));
        request.setLongitude(new BigDecimal("-46.6"));
        request.setStatus(SensorStatus.MAINTENANCE);
        request.setInstallationDate(LocalDate.of(2025, 5, 1));
        request.setHeatZoneId(99L);

        Sensor created = sensorMapper.toEntity(request);
        assertThat(created.getHeatZone()).isNull();
        assertThat(created.getId()).isNull();
        assertThat(created.getStatus()).isEqualTo(SensorStatus.MAINTENANCE);

        HeatZone managedZone = zone(1L, "Centro");
        Sensor existing = new Sensor();
        existing.setId(5L);
        existing.setHeatZone(managedZone);
        sensorMapper.updateEntity(request, existing);
        assertThat(existing.getId()).isEqualTo(5L);
        assertThat(existing.getHeatZone()).isSameAs(managedZone);
        assertThat(managedZone.getId()).isEqualTo(1L);
        assertThat(existing.getCode()).isEqualTo("SP-1");
    }

    @Test
    void sensorResponseExposesZoneIdAndName() {
        Sensor sensor = new Sensor();
        sensor.setId(3L);
        sensor.setCode("SP-3");
        sensor.setHeatZone(zone(7L, "Sé"));

        var response = sensorMapper.toResponse(sensor);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getHeatZoneId()).isEqualTo(7L);
        assertThat(response.getHeatZoneName()).isEqualTo("Sé");
    }

    @Test
    void readingMappingsHandleSensorRelation() {
        TemperatureReadingRequest request = new TemperatureReadingRequest();
        request.setSensorId(42L);
        request.setMeasuredAt(Instant.parse("2026-01-01T10:00:00Z"));
        request.setTemperatureCelsius(new BigDecimal("31.20"));
        request.setHumidityPercent(new BigDecimal("40.00"));
        request.setSource(ReadingSource.MANUAL);

        TemperatureReading entity = readingMapper.toEntity(request);
        assertThat(entity.getSensor()).isNull();
        assertThat(entity.getTemperatureCelsius()).isEqualByComparingTo("31.20");

        Sensor sensor = new Sensor();
        sensor.setId(42L);
        sensor.setCode("SP-42");
        entity.setSensor(sensor);
        var response = readingMapper.toResponse(entity);
        assertThat(response.getSensorId()).isEqualTo(42L);
        assertThat(response.getSensorCode()).isEqualTo("SP-42");
        assertThat(response.getSource()).isEqualTo(ReadingSource.MANUAL);
    }

    @Test
    void coolingPointWithoutZoneMapsNullRelationFields() {
        CoolingPointRequest request = new CoolingPointRequest();
        request.setName("Biblioteca");
        request.setCapacity(10);
        request.setHeatZoneId(3L);

        CoolingPoint entity = coolingPointMapper.toEntity(request);
        assertThat(entity.getHeatZone()).isNull();

        var response = coolingPointMapper.toResponse(entity);
        assertThat(response.getName()).isEqualTo("Biblioteca");
        assertThat(response.getCapacity()).isEqualTo(10);
        assertThat(response.getHeatZoneId()).isNull();
        assertThat(response.getHeatZoneName()).isNull();
    }

    private static HeatZone zone(Long id, String name) {
        HeatZone zone = new HeatZone();
        zone.setId(id);
        zone.setName(name);
        return zone;
    }
}
