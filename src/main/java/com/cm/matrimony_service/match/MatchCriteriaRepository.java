package com.cm.matrimony_service.match;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link MatchCriteria} entities.
 */
public interface MatchCriteriaRepository extends JpaRepository<MatchCriteria, UUID> {
	/**
	 * Finds the match criteria by the associated user's ID.
	 *
	 * @param userId the user ID
	 * @return an optional containing the match criteria if found
	 */
	Optional<MatchCriteria> findByUserId(UUID userId);
}
