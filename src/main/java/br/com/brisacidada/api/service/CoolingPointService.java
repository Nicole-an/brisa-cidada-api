package br.com.brisacidada.api.service;

import br.com.brisacidada.api.dto.request.CoolingPointRequest;
import br.com.brisacidada.api.dto.response.CoolingPointResponse;
import br.com.brisacidada.api.dto.response.PageResponse;
import br.com.brisacidada.api.entity.CoolingPoint;
import br.com.brisacidada.api.exception.ConflictException;
import br.com.brisacidada.api.exception.ResourceNotFoundException;
import br.com.brisacidada.api.mapper.CoolingPointMapper;
import br.com.brisacidada.api.repository.CoolingPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoolingPointService {

    private final CoolingPointRepository coolingPointRepository;
    private final HeatZoneService heatZoneService;
    private final CoolingPointMapper mapper;

    @Transactional
    public CoolingPointResponse create(CoolingPointRequest request) {
        if (coolingPointRepository.existsByNameIgnoreCaseAndAddressIgnoreCase(request.getName(), request.getAddress())) {
            throw duplicated();
        }
        CoolingPoint point = mapper.toEntity(request);
        applyRelationsAndDefaults(point, request);
        return mapper.toResponse(coolingPointRepository.saveAndFlush(point));
    }

    public PageResponse<CoolingPointResponse> findAll(Long heatZoneId, Pageable pageable) {
        Page<CoolingPoint> page = heatZoneId == null
                ? coolingPointRepository.findAll(pageable)
                : coolingPointRepository.findByHeatZoneId(heatZoneId, pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    public CoolingPointResponse findById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public CoolingPointResponse update(Long id, CoolingPointRequest request) {
        CoolingPoint point = getEntity(id);
        if (coolingPointRepository.existsByNameIgnoreCaseAndAddressIgnoreCaseAndIdNot(
                request.getName(), request.getAddress(), id)) {
            throw duplicated();
        }
        mapper.updateEntity(request, point);
        applyRelationsAndDefaults(point, request);
        return mapper.toResponse(coolingPointRepository.saveAndFlush(point));
    }

    @Transactional
    public void delete(Long id) {
        coolingPointRepository.delete(getEntity(id));
    }

    private CoolingPoint getEntity(Long id) {
        return coolingPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de resfriamento", id));
    }

    private void applyRelationsAndDefaults(CoolingPoint point, CoolingPointRequest request) {
        point.setHeatZone(request.getHeatZoneId() == null ? null : heatZoneService.getEntity(request.getHeatZoneId()));
        if (point.getActive() == null) {
            point.setActive(Boolean.TRUE);
        }
    }

    private static ConflictException duplicated() {
        return new ConflictException("Já existe um ponto de resfriamento com este nome neste endereço.");
    }
}
