package com.cm.matrimony_service.subscription;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing ContactViewLog entities.
 */
public interface ContactViewLogRepository extends JpaRepository<ContactViewLog, UUID> {

	/**
	 * Finds a contact view log by the viewer's ID and the viewed user's ID.
	 *
	 * @param viewerId the ID of the user who viewed the contact details
	 * @param viewedUserId the ID of the user whose contact details were viewed
	 * @return an Optional containing the ContactViewLog if found
	 */
	Optional<ContactViewLog> findByViewerIdAndViewedUserId(UUID viewerId, UUID viewedUserId);

	/**
	 * Checks if a contact view log exists for a specific viewer and viewed user.
	 *
	 * @param viewerId the ID of the user who viewed the contact details
	 * @param viewedUserId the ID of the user whose contact details were viewed
	 * @return true if the log exists, false otherwise
	 */
	boolean existsByViewerIdAndViewedUserId(UUID viewerId, UUID viewedUserId);
}
