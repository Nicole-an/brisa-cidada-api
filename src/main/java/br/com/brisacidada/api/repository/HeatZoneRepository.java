package br.com.brisacidada.api.repository;

import br.com.brisacidada.api.entity.HeatZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeatZoneRepository extends JpaRepository<HeatZone, Long> {

    boolean existsByNameIgnoreCaseAndCityIgnoreCase(String name, String city);

    boolean existsByNameIgnoreCaseAndCityIgnoreCaseAndIdNot(String name, String city, Long id);
}
