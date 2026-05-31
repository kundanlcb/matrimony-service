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

@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
public class InteractionController {

	private final InteractionService interactionService;

	@PostMapping
	SendInteractionResponse send(@AuthenticationPrincipal AuthenticatedUser user,
		@Valid @RequestBody SendInteractionRequest request) {
		return interactionService.send(user.id(), request.toUserId(), request.type());
	}

	@GetMapping("/received")
	List<BiodataResponse> received(@AuthenticationPrincipal AuthenticatedUser user) {
		return interactionService.received(user.id());
	}

	@GetMapping("/sent")
	List<BiodataResponse> sent(@AuthenticationPrincipal AuthenticatedUser user) {
		return interactionService.sent(user.id());
	}

	@GetMapping("/matches")
	List<BiodataResponse> matches(@AuthenticationPrincipal AuthenticatedUser user) {
		return interactionService.matches(user.id());
	}
}
