package com.cm.matrimony_service.biodata;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiodataRepository extends JpaRepository<Biodata, UUID> {
	Optional<Biodata> findByUserId(UUID userId);
}
