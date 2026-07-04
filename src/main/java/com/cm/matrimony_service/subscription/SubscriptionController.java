package com.cm.matrimony_service.subscription;

import com.cm.matrimony_service.common.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for managing subscription purchases, status retrieval,
 * and unlocking/revealing contact details.
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	/**
	 * Purchases a new subscription plan or credit pack.
	 *
	 * @param user the authenticated user
	 * @param request the purchase request details
	 * @return a response containing the purchase status and subscription ID
	 */
	@PostMapping("/purchase")
	public SubscriptionDtos.PurchaseResponse purchase(
		@AuthenticationPrincipal AuthenticatedUser user,
		@Valid @RequestBody SubscriptionDtos.PurchaseRequest request) {
		return subscriptionService.purchase(user.id(), request);
	}

	/**
	 * Retrieves the current subscription status and remaining credits of the authenticated user.
	 *
	 * @param user the authenticated user
	 * @return the subscription status response
	 */
	@GetMapping("/status")
	public SubscriptionDtos.SubscriptionStatusResponse getStatus(
		@AuthenticationPrincipal AuthenticatedUser user) {
		return subscriptionService.getStatus(user.id());
	}

	/**
	 * Reveals the contact details of a target user using an active subscription or credits.
	 *
	 * @param user the authenticated user initiating the reveal
	 * @param targetUserId the ID of the user whose contact details are to be revealed
	 * @return a response indicating success or failure of the reveal action
	 */
	@PostMapping("/reveal/{targetUserId}")
	public SubscriptionDtos.RevealResponse reveal(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID targetUserId) {
		return subscriptionService.reveal(user.id(), targetUserId);
	}
}
