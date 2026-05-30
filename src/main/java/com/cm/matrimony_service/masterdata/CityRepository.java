package com.cm.matrimony_service.masterdata;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, UUID> {
}
