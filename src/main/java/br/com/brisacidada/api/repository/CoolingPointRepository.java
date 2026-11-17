package br.com.brisacidada.api.repository;

import br.com.brisacidada.api.entity.CoolingPoint;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoolingPointRepository extends JpaRepository<CoolingPoint, Long> {

    Page<CoolingPoint> findByHeatZoneId(Long heatZoneId, Pageable pageable);

    List<CoolingPoint> findByHeatZoneIdAndActiveTrue(Long heatZoneId, Pageable pageable);

    boolean existsByHeatZoneId(Long heatZoneId);

    boolean existsByNameIgnoreCaseAndAddressIgnoreCase(String name, String address);

    boolean existsByNameIgnoreCaseAndAddressIgnoreCaseAndIdNot(String name, String address, Long id);
}
