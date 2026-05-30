package com.cm.matrimony_service.subscription;

import com.cm.matrimony_service.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class Subscription {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "plan_type", nullable = false, length = 20)
	private PlanType planType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

	@Column(name = "start_date", nullable = false)
	private Instant startDate;

	@Column(name = "end_date")
	private Instant endDate;

	@Column(name = "credits_purchased")
	private Integer creditsPurchased = 0;

	@Column(name = "credits_remaining")
	private Integer creditsRemaining = 0;

	@Column(name = "payment_ref", length = 100)
	private String paymentRef;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public Subscription(User user, PlanType planType, Instant startDate, Instant endDate, Integer creditsPurchased, Integer creditsRemaining, String paymentRef) {
		this.user = user;
		this.planType = planType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.creditsPurchased = creditsPurchased;
		this.creditsRemaining = creditsRemaining;
		this.paymentRef = paymentRef;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
