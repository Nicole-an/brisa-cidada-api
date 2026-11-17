package br.com.brisacidada.api.mapper;

import br.com.brisacidada.api.dto.request.SensorRequest;
import br.com.brisacidada.api.dto.response.SensorResponse;
import br.com.brisacidada.api.entity.Sensor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SensorMapper {

    private final ModelMapper modelMapper;

    public Sensor toEntity(SensorRequest request) {
        return modelMapper.map(request, Sensor.class);
    }

    public void updateEntity(SensorRequest request, Sensor entity) {
        modelMapper.map(request, entity);
    }

    public SensorResponse toResponse(Sensor entity) {
        return modelMapper.map(entity, SensorResponse.class);
    }
}
