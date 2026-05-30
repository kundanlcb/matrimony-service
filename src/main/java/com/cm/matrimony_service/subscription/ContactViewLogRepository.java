package com.cm.matrimony_service.subscription;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactViewLogRepository extends JpaRepository<ContactViewLog, UUID> {

	Optional<ContactViewLog> findByViewerIdAndViewedUserId(UUID viewerId, UUID viewedUserId);

	boolean existsByViewerIdAndViewedUserId(UUID viewerId, UUID viewedUserId);
}
