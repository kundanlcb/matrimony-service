package com.cm.matrimony_service.masterdata;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Profession} entity.
 */
public interface ProfessionRepository extends JpaRepository<Profession, UUID> {
}
