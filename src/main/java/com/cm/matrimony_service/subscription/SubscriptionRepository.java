package com.cm.matrimony_service.subscription;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

	@Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.planType = 'MONTHLY' AND s.status = 'ACTIVE' AND s.endDate > :now")
	List<Subscription> findActiveMonthlySubscriptions(@Param("userId") UUID userId, @Param("now") Instant now);

	@Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.planType = 'PAY_PER_CONTACT' AND s.creditsRemaining > 0")
	List<Subscription> findActiveCreditSubscriptions(@Param("userId") UUID userId);
}
