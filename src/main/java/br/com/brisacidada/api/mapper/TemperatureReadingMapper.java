package br.com.brisacidada.api.mapper;

import br.com.brisacidada.api.dto.request.TemperatureReadingRequest;
import br.com.brisacidada.api.dto.response.TemperatureReadingResponse;
import br.com.brisacidada.api.entity.TemperatureReading;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemperatureReadingMapper {

    private final ModelMapper modelMapper;

    public TemperatureReading toEntity(TemperatureReadingRequest request) {
        return modelMapper.map(request, TemperatureReading.class);
    }

    public void updateEntity(TemperatureReadingRequest request, TemperatureReading entity) {
        modelMapper.map(request, entity);
    }

    public TemperatureReadingResponse toResponse(TemperatureReading entity) {
        return modelMapper.map(entity, TemperatureReadingResponse.class);
    }
}
