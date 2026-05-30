package com.cm.matrimony_service.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockRepository extends JpaRepository<Block, UUID> {

	Optional<Block> findByFromUserIdAndToUserId(UUID fromUserId, UUID toUserId);

	List<Block> findByFromUserId(UUID fromUserId);

	@Query("SELECT b.toUser.id FROM Block b WHERE b.fromUser.id = :userId UNION SELECT b.fromUser.id FROM Block b WHERE b.toUser.id = :userId")
	List<UUID> findBlockedUserIds(@Param("userId") UUID userId);

	@Query("SELECT COUNT(b) > 0 FROM Block b WHERE (b.fromUser.id = :user1 AND b.toUser.id = :user2) OR (b.fromUser.id = :user2 AND b.toUser.id = :user1)")
	boolean existsBlockBetween(@Param("user1") UUID user1, @Param("user2") UUID user2);
}
