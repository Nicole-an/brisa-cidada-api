package br.com.brisacidada.api.config;

import br.com.brisacidada.api.dto.request.CoolingPointRequest;
import br.com.brisacidada.api.dto.request.SensorRequest;
import br.com.brisacidada.api.dto.request.TemperatureReadingRequest;
import br.com.brisacidada.api.dto.response.CoolingPointResponse;
import br.com.brisacidada.api.dto.response.SensorResponse;
import br.com.brisacidada.api.dto.response.TemperatureReadingResponse;
import br.com.brisacidada.api.entity.CoolingPoint;
import br.com.brisacidada.api.entity.Sensor;
import br.com.brisacidada.api.entity.TemperatureReading;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(false);

        // Relacionamentos são resolvidos pelos services; nunca a partir de IDs recebidos na requisição.
        modelMapper.typeMap(SensorRequest.class, Sensor.class)
                .addMappings(m -> m.skip(Sensor::setHeatZone));
        modelMapper.typeMap(TemperatureReadingRequest.class, TemperatureReading.class)
                .addMappings(m -> m.skip(TemperatureReading::setSensor));
        modelMapper.typeMap(CoolingPointRequest.class, CoolingPoint.class)
                .addMappings(m -> m.skip(CoolingPoint::setHeatZone));

        modelMapper.typeMap(Sensor.class, SensorResponse.class)
                .addMappings(m -> {
                    m.map(src -> src.getHeatZone().getId(), SensorResponse::setHeatZoneId);
                    m.map(src -> src.getHeatZone().getName(), SensorResponse::setHeatZoneName);
                });
        modelMapper.typeMap(TemperatureReading.class, TemperatureReadingResponse.class)
                .addMappings(m -> {
                    m.map(src -> src.getSensor().getId(), TemperatureReadingResponse::setSensorId);
                    m.map(src -> src.getSensor().getCode(), TemperatureReadingResponse::setSensorCode);
                });
        modelMapper.typeMap(CoolingPoint.class, CoolingPointResponse.class)
                .addMappings(m -> {
                    m.map(src -> src.getHeatZone().getId(), CoolingPointResponse::setHeatZoneId);
                    m.map(src -> src.getHeatZone().getName(), CoolingPointResponse::setHeatZoneName);
                });

        return modelMapper;
    }
}
