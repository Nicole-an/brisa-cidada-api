package br.com.brisacidada.api.mapper;

import br.com.brisacidada.api.dto.request.CoolingPointRequest;
import br.com.brisacidada.api.dto.response.CoolingPointResponse;
import br.com.brisacidada.api.entity.CoolingPoint;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoolingPointMapper {

    private final ModelMapper modelMapper;

    public CoolingPoint toEntity(CoolingPointRequest request) {
        return modelMapper.map(request, CoolingPoint.class);
    }

    public void updateEntity(CoolingPointRequest request, CoolingPoint entity) {
        modelMapper.map(request, entity);
    }

    public CoolingPointResponse toResponse(CoolingPoint entity) {
        return modelMapper.map(entity, CoolingPointResponse.class);
    }
}
