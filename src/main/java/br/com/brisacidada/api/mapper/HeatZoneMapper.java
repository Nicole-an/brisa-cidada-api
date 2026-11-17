package br.com.brisacidada.api.mapper;

import br.com.brisacidada.api.dto.request.HeatZoneRequest;
import br.com.brisacidada.api.dto.response.HeatZoneResponse;
import br.com.brisacidada.api.entity.HeatZone;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeatZoneMapper {

    private final ModelMapper modelMapper;

    public HeatZone toEntity(HeatZoneRequest request) {
        return modelMapper.map(request, HeatZone.class);
    }

    public void updateEntity(HeatZoneRequest request, HeatZone entity) {
        modelMapper.map(request, entity);
    }

    public HeatZoneResponse toResponse(HeatZone entity) {
        return modelMapper.map(entity, HeatZoneResponse.class);
    }
}
