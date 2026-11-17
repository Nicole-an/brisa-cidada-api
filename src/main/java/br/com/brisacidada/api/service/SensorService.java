package br.com.brisacidada.api.service;

import br.com.brisacidada.api.dto.request.SensorRequest;
import br.com.brisacidada.api.dto.response.PageResponse;
import br.com.brisacidada.api.dto.response.SensorResponse;
import br.com.brisacidada.api.entity.HeatZone;
import br.com.brisacidada.api.entity.Sensor;
import br.com.brisacidada.api.exception.ConflictException;
import br.com.brisacidada.api.exception.ResourceNotFoundException;
import br.com.brisacidada.api.mapper.SensorMapper;
import br.com.brisacidada.api.repository.SensorRepository;
import br.com.brisacidada.api.repository.TemperatureReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorService {

    private final SensorRepository sensorRepository;
    private final TemperatureReadingRepository readingRepository;
    private final HeatZoneService heatZoneService;
    private final SensorMapper mapper;

    @Transactional
    public SensorResponse create(SensorRequest request) {
        HeatZone zone = heatZoneService.getEntity(request.getHeatZoneId());
        if (sensorRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw duplicated();
        }
        Sensor sensor = mapper.toEntity(request);
        sensor.setHeatZone(zone);
        return mapper.toResponse(sensorRepository.saveAndFlush(sensor));
    }

    public PageResponse<SensorResponse> findAll(Long heatZoneId, Pageable pageable) {
        Page<Sensor> page = heatZoneId == null
                ? sensorRepository.findAll(pageable)
                : sensorRepository.findByHeatZoneId(heatZoneId, pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    public SensorResponse findById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public SensorResponse update(Long id, SensorRequest request) {
        Sensor sensor = getEntity(id);
        HeatZone zone = heatZoneService.getEntity(request.getHeatZoneId());
        if (sensorRepository.existsByCodeIgnoreCaseAndIdNot(request.getCode(), id)) {
            throw duplicated();
        }
        mapper.updateEntity(request, sensor);
        sensor.setHeatZone(zone);
        return mapper.toResponse(sensorRepository.saveAndFlush(sensor));
    }

    @Transactional
    public void delete(Long id) {
        Sensor sensor = getEntity(id);
        if (readingRepository.existsBySensorId(id)) {
            throw new ConflictException("O sensor possui leituras registradas; remova-as antes de excluir o sensor.");
        }
        sensorRepository.delete(sensor);
    }

    public Sensor getEntity(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }

    private static ConflictException duplicated() {
        return new ConflictException("Já existe um sensor com este código.");
    }
}
