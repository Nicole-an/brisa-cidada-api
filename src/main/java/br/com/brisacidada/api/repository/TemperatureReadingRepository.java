package br.com.brisacidada.api.repository;

import br.com.brisacidada.api.entity.ReadingQuality;
import br.com.brisacidada.api.entity.TemperatureReading;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureReadingRepository extends JpaRepository<TemperatureReading, Long> {

    Page<TemperatureReading> findBySensorId(Long sensorId, Pageable pageable);

    boolean existsBySensorId(Long sensorId);

    boolean existsBySensorIdAndMeasuredAt(Long sensorId, Instant measuredAt);

    boolean existsBySensorIdAndMeasuredAtAndIdNot(Long sensorId, Instant measuredAt, Long id);

    List<TemperatureReading> findBySensorHeatZoneIdAndMeasuredAtBetweenAndQualityNot(
            Long heatZoneId, Instant from, Instant to, ReadingQuality excludedQuality);
}
