package com.cm.matrimony_service.subscription;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public final class SubscriptionDtos {

	private SubscriptionDtos() {
	}

	public record PurchaseRequest(
		@NotBlank String planType,
		Integer credits,
		@NotBlank String paymentRef) {
	}

	public record PurchaseResponse(
		String status,
		String message,
		UUID subscriptionId) {
	}

	public record SubscriptionStatusResponse(
		String planType,
		String status,
		Integer creditsRemaining,
		Instant endDate) {
	}

	public record RevealRequest(
		UUID targetUserId) {
	}

	public record RevealResponse(
		String status,
		String message,
		boolean revealed) {
	}
}
