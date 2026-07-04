package com.cm.matrimony_service.interaction;

import com.cm.matrimony_service.biodata.BiodataDtos.BiodataResponse;
import com.cm.matrimony_service.common.security.AuthenticatedUser;
import com.cm.matrimony_service.interaction.InteractionDtos.SendInteractionRequest;
import com.cm.matrimony_service.interaction.InteractionDtos.SendInteractionResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing user interactions.
 * Provides endpoints for sending interests, and retrieving received, sent, and matched profiles.
 */
@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
public class InteractionController {

	private final InteractionService interactionService;

	/**
	 * Endpoint to send an interaction to another user.
	 *
	 * @param user    the currently authenticated user
	 * @param request the request body containing the target user ID and interaction type
	 * @return a response containing the interaction status and whether it resulted in a mutual match
	 */
	@PostMapping
	SendInteractionResponse send(@AuthenticationPrincipal AuthenticatedUser user,
		@Valid @RequestBody SendInteractionRequest request) {
		return interactionService.send(user.id(), request.toUserId(), request.type());
	}

	/**
	 * Endpoint to retrieve all profiles that have sent an interest to the current user.
	 *
	 * @param user the currently authenticated user
	 * @return a list of biodata responses for profiles that sent an interest
	 */
	@GetMapping("/received")
	List<BiodataResponse> received(@AuthenticationPrincipal AuthenticatedUser user) {
		return interactionService.received(user.id());
	}

	/**
	 * Endpoint to retrieve all profiles that the current user has sent an interest to.
	 *
	 * @param user the currently authenticated user
	 * @return a list of biodata responses for profiles the user sent an interest to
	 */
	@GetMapping("/sent")
	List<BiodataResponse> sent(@AuthenticationPrincipal AuthenticatedUser user) {
		return interactionService.sent(user.id());
	}

	/**
	 * Endpoint to retrieve all mutual matches for the current user.
	 *
	 * @param user the currently authenticated user
	 * @return a list of biodata responses for mutual matches
	 */
	@GetMapping("/matches")
	List<BiodataResponse> matches(@AuthenticationPrincipal AuthenticatedUser user) {
		return interactionService.matches(user.id());
	}
}
