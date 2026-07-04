package com.cm.matrimony_service.masterdata;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Gotra} entity.
 */
public interface GotraRepository extends JpaRepository<Gotra, UUID> {
}
