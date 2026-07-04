package com.cm.matrimony_service.masterdata;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link City} entity.
 */
public interface CityRepository extends JpaRepository<City, UUID> {
}
