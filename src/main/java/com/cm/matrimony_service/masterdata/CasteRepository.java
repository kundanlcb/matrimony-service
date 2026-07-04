package com.cm.matrimony_service.masterdata;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Caste} entity.
 */
public interface CasteRepository extends JpaRepository<Caste, UUID> {
}
