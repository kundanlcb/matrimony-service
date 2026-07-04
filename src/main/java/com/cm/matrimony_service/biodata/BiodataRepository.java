package com.cm.matrimony_service.biodata;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link Biodata} entities.
 */
public interface BiodataRepository extends JpaRepository<Biodata, UUID> {
	/**
	 * Finds a biodata by the associated user's ID.
	 *
	 * @param userId the user ID
	 * @return an optional containing the biodata if found
	 */
	Optional<Biodata> findByUserId(UUID userId);
}
