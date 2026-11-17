package br.com.brisacidada.api.service;

import br.com.brisacidada.api.dto.request.HeatZoneRequest;
import br.com.brisacidada.api.dto.response.HeatZoneResponse;
import br.com.brisacidada.api.dto.response.PageResponse;
import br.com.brisacidada.api.entity.HeatZone;
import br.com.brisacidada.api.exception.ConflictException;
import br.com.brisacidada.api.exception.ResourceNotFoundException;
import br.com.brisacidada.api.mapper.HeatZoneMapper;
import br.com.brisacidada.api.repository.CoolingPointRepository;
import br.com.brisacidada.api.repository.HeatZoneRepository;
import br.com.brisacidada.api.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeatZoneService {

    private final HeatZoneRepository heatZoneRepository;
    private final SensorRepository sensorRepository;
    private final CoolingPointRepository coolingPointRepository;
    private final HeatZoneMapper mapper;

    @Transactional
    public HeatZoneResponse create(HeatZoneRequest request) {
        if (heatZoneRepository.existsByNameIgnoreCaseAndCityIgnoreCase(request.getName(), request.getCity())) {
            throw duplicated();
        }
        HeatZone zone = mapper.toEntity(request);
        applyDefaults(zone);
        return mapper.toResponse(heatZoneRepository.saveAndFlush(zone));
    }

    public PageResponse<HeatZoneResponse> findAll(Pageable pageable) {
        return PageResponse.from(heatZoneRepository.findAll(pageable), mapper::toResponse);
    }

    public HeatZoneResponse findById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public HeatZoneResponse update(Long id, HeatZoneRequest request) {
        HeatZone zone = getEntity(id);
        if (heatZoneRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndIdNot(request.getName(), request.getCity(), id)) {
            throw duplicated();
        }
        mapper.updateEntity(request, zone);
        applyDefaults(zone);
        return mapper.toResponse(heatZoneRepository.saveAndFlush(zone));
    }

    @Transactional
    public void delete(Long id) {
        HeatZone zone = getEntity(id);
        if (sensorRepository.existsByHeatZoneId(id) || coolingPointRepository.existsByHeatZoneId(id)) {
            throw new ConflictException(
                    "A zona de calor possui sensores ou pontos de resfriamento vinculados; remova-os ou desvincule-os antes.");
        }
        heatZoneRepository.delete(zone);
    }

    public HeatZone getEntity(Long id) {
        return heatZoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona de calor", id));
    }

    private static void applyDefaults(HeatZone zone) {
        if (zone.getActive() == null) {
            zone.setActive(Boolean.TRUE);
        }
    }

    private static ConflictException duplicated() {
        return new ConflictException("Já existe uma zona de calor com este nome nesta cidade.");
    }
}
