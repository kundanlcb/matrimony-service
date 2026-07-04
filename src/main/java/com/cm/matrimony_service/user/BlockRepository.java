package com.cm.matrimony_service.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing Block entities.
 */
public interface BlockRepository extends JpaRepository<Block, UUID> {

	/**
	 * Finds a block by the user who blocked and the user who is blocked.
	 *
	 * @param fromUserId the ID of the user who initiated the block
	 * @param toUserId the ID of the user who is blocked
	 * @return an Optional containing the block if found
	 */
	Optional<Block> findByFromUserIdAndToUserId(UUID fromUserId, UUID toUserId);

	/**
	 * Finds all blocks initiated by a specific user.
	 *
	 * @param fromUserId the ID of the user who initiated the blocks
	 * @return a list of blocks
	 */
	List<Block> findByFromUserId(UUID fromUserId);

	/**
	 * Finds the IDs of all users involved in a block with the specified user,
	 * either as the blocker or the blocked user.
	 *
	 * @param userId the ID of the user
	 * @return a list of user IDs
	 */
	@Query("SELECT b.toUser.id FROM Block b WHERE b.fromUser.id = :userId UNION SELECT b.fromUser.id FROM Block b WHERE b.toUser.id = :userId")
	List<UUID> findBlockedUserIds(@Param("userId") UUID userId);

	/**
	 * Checks if a block exists between two users in either direction.
	 *
	 * @param user1 the ID of the first user
	 * @param user2 the ID of the second user
	 * @return true if a block exists, false otherwise
	 */
	@Query("SELECT COUNT(b) > 0 FROM Block b WHERE (b.fromUser.id = :user1 AND b.toUser.id = :user2) OR (b.fromUser.id = :user2 AND b.toUser.id = :user1)")
	boolean existsBlockBetween(@Param("user1") UUID user1, @Param("user2") UUID user2);
}
