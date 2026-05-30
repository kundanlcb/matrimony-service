package com.cm.matrimony_service.match;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchCriteriaRepository extends JpaRepository<MatchCriteria, UUID> {
	Optional<MatchCriteria> findByUserId(UUID userId);
}
