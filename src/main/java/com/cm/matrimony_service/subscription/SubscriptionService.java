package com.cm.matrimony_service.subscription;

import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.interaction.InteractionRepository;
import com.cm.matrimony_service.interaction.InteractionType;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;
	private final ContactViewLogRepository contactViewLogRepository;
	private final UserRepository userRepository;
	private final InteractionRepository interactionRepository;

	@Transactional
	public SubscriptionDtos.PurchaseResponse purchase(UUID userId, SubscriptionDtos.PurchaseRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

		PlanType planType;
		try {
			planType = PlanType.valueOf(request.planType().trim().toUpperCase());
		} catch (Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid plan type");
		}

		Subscription subscription;
		Instant now = Instant.now();

		if (planType == PlanType.MONTHLY) {
			Instant endDate = now.plus(30, ChronoUnit.DAYS);
			subscription = new Subscription(user, planType, now, endDate, 0, 0, request.paymentRef());
		} else {
			int credits = request.credits() != null ? request.credits() : 5;
			subscription = new Subscription(user, planType, now, null, credits, credits, request.paymentRef());
		}

		Subscription saved = subscriptionRepository.save(subscription);
		return new SubscriptionDtos.PurchaseResponse("success", "Premium plan activated successfully", saved.getId());
	}

	@Transactional(readOnly = true)
	public SubscriptionDtos.SubscriptionStatusResponse getStatus(UUID userId) {
		Instant now = Instant.now();
		List<Subscription> monthly = subscriptionRepository.findActiveMonthlySubscriptions(userId, now);
		if (!monthly.isEmpty()) {
			Subscription sub = monthly.get(0);
			return new SubscriptionDtos.SubscriptionStatusResponse("monthly", "active", 0, sub.getEndDate());
		}

		List<Subscription> credits = subscriptionRepository.findActiveCreditSubscriptions(userId);
		int totalCredits = credits.stream().mapToInt(Subscription::getCreditsRemaining).sum();
		if (totalCredits > 0) {
			return new SubscriptionDtos.SubscriptionStatusResponse("pay_per_contact", "active", totalCredits, null);
		}

		return new SubscriptionDtos.SubscriptionStatusResponse("free", "active", 0, null);
	}

	@Transactional(readOnly = true)
	public boolean isUnlocked(UUID viewerId, UUID targetUserId) {
		if (viewerId.equals(targetUserId)) {
			return true;
		}

		// Check mutual interest (hard gate)
		boolean hasMutualInterest = interactionRepository.findByFromUserIdAndToUserIdAndType(viewerId, targetUserId, InteractionType.MATCH_ACCEPTED).isPresent()
			|| interactionRepository.findByFromUserIdAndToUserIdAndType(targetUserId, viewerId, InteractionType.MATCH_ACCEPTED).isPresent();

		if (!hasMutualInterest) {
			return false;
		}

		// Check if active monthly subscription exists
		Instant now = Instant.now();
		List<Subscription> activeMonthly = subscriptionRepository.findActiveMonthlySubscriptions(viewerId, now);
		if (!activeMonthly.isEmpty()) {
			return true;
		}

		// Check if revealed via credits view log
		return contactViewLogRepository.existsByViewerIdAndViewedUserId(viewerId, targetUserId);
	}

	@Transactional
	public SubscriptionDtos.RevealResponse reveal(UUID viewerId, UUID targetUserId) {
		if (viewerId.equals(targetUserId)) {
			return new SubscriptionDtos.RevealResponse("success", "Own contact details revealed", true);
		}

		// Check mutual interest (hard gate)
		boolean hasMutualInterest = interactionRepository.findByFromUserIdAndToUserIdAndType(viewerId, targetUserId, InteractionType.MATCH_ACCEPTED).isPresent()
			|| interactionRepository.findByFromUserIdAndToUserIdAndType(targetUserId, viewerId, InteractionType.MATCH_ACCEPTED).isPresent();

		if (!hasMutualInterest) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Mutual interest matches are required to unlock contact details");
		}

		// Check if already unlocked
		if (isUnlocked(viewerId, targetUserId)) {
			return new SubscriptionDtos.RevealResponse("success", "Contact details already revealed", true);
		}

		// Find active credit subscriptions
		List<Subscription> creditSubs = subscriptionRepository.findActiveCreditSubscriptions(viewerId);
		if (creditSubs.isEmpty()) {
			throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Insufficient credits. Please upgrade to a premium plan to reveal contact details.");
		}

		// Deduct 1 credit from the oldest active pack
		Subscription sub = creditSubs.get(0);
		sub.setCreditsRemaining(sub.getCreditsRemaining() - 1);
		subscriptionRepository.save(sub);

		// Log the contact view unlock
		User viewer = userRepository.findById(viewerId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		User target = userRepository.findById(targetUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target user not found"));

		contactViewLogRepository.save(new ContactViewLog(viewer, target));

		return new SubscriptionDtos.RevealResponse("success", "Contact details revealed successfully", true);
	}
}
