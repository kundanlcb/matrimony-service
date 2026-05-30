package com.cm.matrimony_service.interaction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
	Optional<Interaction> findByFromUserIdAndToUserIdAndType(UUID fromUserId, UUID toUserId, InteractionType type);

	List<Interaction> findByToUserIdAndType(UUID toUserId, InteractionType type);

	@Query("select i.toUser.id from Interaction i where i.fromUser.id = :userId and i.type in :types")
	List<UUID> findInteractedUserIds(@Param("userId") UUID userId, @Param("types") Collection<InteractionType> types);

	@org.springframework.data.jpa.repository.Modifying
	@Query("delete from Interaction i where i.fromUser.id = :userId or i.toUser.id = :userId")
	void deleteAllByUserId(@Param("userId") UUID userId);

	@org.springframework.data.jpa.repository.Modifying
	@Query("delete from Interaction i where (i.fromUser.id = :user1 and i.toUser.id = :user2) or (i.fromUser.id = :user2 and i.toUser.id = :user1)")
	void deleteInteractionsBetween(@Param("user1") UUID user1, @Param("user2") UUID user2);
}
