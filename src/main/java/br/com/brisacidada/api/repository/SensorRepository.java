package br.com.brisacidada.api.repository;

import br.com.brisacidada.api.entity.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Page<Sensor> findByHeatZoneId(Long heatZoneId, Pageable pageable);

    boolean existsByHeatZoneId(Long heatZoneId);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
