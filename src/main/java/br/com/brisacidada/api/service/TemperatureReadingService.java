package br.com.brisacidada.api.service;

import br.com.brisacidada.api.dto.request.TemperatureReadingRequest;
import br.com.brisacidada.api.dto.response.PageResponse;
import br.com.brisacidada.api.dto.response.TemperatureReadingResponse;
import br.com.brisacidada.api.entity.ReadingQuality;
import br.com.brisacidada.api.entity.Sensor;
import br.com.brisacidada.api.entity.TemperatureReading;
import br.com.brisacidada.api.exception.ConflictException;
import br.com.brisacidada.api.exception.ResourceNotFoundException;
import br.com.brisacidada.api.mapper.TemperatureReadingMapper;
import br.com.brisacidada.api.repository.TemperatureReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemperatureReadingService {

    private final TemperatureReadingRepository readingRepository;
    private final SensorService sensorService;
    private final TemperatureReadingMapper mapper;

    @Transactional
    public TemperatureReadingResponse create(TemperatureReadingRequest request) {
        Sensor sensor = sensorService.getEntity(request.getSensorId());
        if (readingRepository.existsBySensorIdAndMeasuredAt(sensor.getId(), request.getMeasuredAt())) {
            throw duplicated();
        }
        TemperatureReading reading = mapper.toEntity(request);
        reading.setSensor(sensor);
        applyDefaults(reading);
        TemperatureReading saved = readingRepository.saveAndFlush(reading);
        registerCommunication(sensor, saved);
        return mapper.toResponse(saved);
    }

    public PageResponse<TemperatureReadingResponse> findAll(Long sensorId, Pageable pageable) {
        Page<TemperatureReading> page = sensorId == null
                ? readingRepository.findAll(pageable)
                : readingRepository.findBySensorId(sensorId, pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    public TemperatureReadingResponse findById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public TemperatureReadingResponse update(Long id, TemperatureReadingRequest request) {
        TemperatureReading reading = getEntity(id);
        Sensor sensor = sensorService.getEntity(request.getSensorId());
        if (readingRepository.existsBySensorIdAndMeasuredAtAndIdNot(sensor.getId(), request.getMeasuredAt(), id)) {
            throw duplicated();
        }
        mapper.updateEntity(request, reading);
        reading.setSensor(sensor);
        applyDefaults(reading);
        TemperatureReading saved = readingRepository.saveAndFlush(reading);
        registerCommunication(sensor, saved);
        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        readingRepository.delete(getEntity(id));
    }

    private TemperatureReading getEntity(Long id) {
        return readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leitura de temperatura", id));
    }

    private static void applyDefaults(TemperatureReading reading) {
        if (reading.getQuality() == null) {
            reading.setQuality(ReadingQuality.VALID);
        }
    }

    private static void registerCommunication(Sensor sensor, TemperatureReading reading) {
        if (sensor.getLastCommunicationAt() == null || reading.getMeasuredAt().isAfter(sensor.getLastCommunicationAt())) {
            sensor.setLastCommunicationAt(reading.getMeasuredAt());
        }
    }

    private static ConflictException duplicated() {
        return new ConflictException("Já existe uma leitura deste sensor para o mesmo instante de medição.");
    }
}
