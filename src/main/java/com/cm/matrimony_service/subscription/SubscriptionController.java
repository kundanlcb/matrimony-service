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

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@PostMapping("/purchase")
	public SubscriptionDtos.PurchaseResponse purchase(
		@AuthenticationPrincipal AuthenticatedUser user,
		@Valid @RequestBody SubscriptionDtos.PurchaseRequest request) {
		return subscriptionService.purchase(user.id(), request);
	}

	@GetMapping("/status")
	public SubscriptionDtos.SubscriptionStatusResponse getStatus(
		@AuthenticationPrincipal AuthenticatedUser user) {
		return subscriptionService.getStatus(user.id());
	}

	@PostMapping("/reveal/{targetUserId}")
	public SubscriptionDtos.RevealResponse reveal(
		@AuthenticationPrincipal AuthenticatedUser user,
		@PathVariable UUID targetUserId) {
		return subscriptionService.reveal(user.id(), targetUserId);
	}
}
