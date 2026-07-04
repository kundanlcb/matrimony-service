package com.cm.matrimony_service.subscription;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing Subscription entities.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	/**
	 * Finds all subscriptions for a specific user, ordered by creation date descending.
	 *
	 * @param userId the ID of the user
	 * @return a list of subscriptions
	 */
	List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

	/**
	 * Finds active monthly subscriptions for a specific user that are currently valid.
	 *
	 * @param userId the ID of the user
	 * @param now the current time to check against the end date
	 * @return a list of active monthly subscriptions
	 */
	@Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.planType = 'MONTHLY' AND s.status = 'ACTIVE' AND s.endDate > :now")
	List<Subscription> findActiveMonthlySubscriptions(@Param("userId") UUID userId, @Param("now") Instant now);

	/**
	 * Finds active pay-per-contact subscriptions for a specific user that have remaining credits.
	 *
	 * @param userId the ID of the user
	 * @return a list of active credit-based subscriptions
	 */
	@Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.planType = 'PAY_PER_CONTACT' AND s.creditsRemaining > 0")
	List<Subscription> findActiveCreditSubscriptions(@Param("userId") UUID userId);
}
