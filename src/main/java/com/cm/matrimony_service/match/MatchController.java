package com.cm.matrimony_service.match;

import com.cm.matrimony_service.common.security.AuthenticatedUser;
import com.cm.matrimony_service.match.MatchDtos.CriteriaRequest;
import com.cm.matrimony_service.match.MatchDtos.CriteriaResponse;
import com.cm.matrimony_service.match.MatchDtos.MatchProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

	private final MatchService matchService;

	@GetMapping
	Page<MatchProfileResponse> findMatches(@AuthenticationPrincipal AuthenticatedUser user,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "score") String sortBy) {
		return matchService.findMatches(user.id(), page, size, sortBy);
	}

	@GetMapping("/criteria")
	CriteriaResponse getCriteria(@AuthenticationPrincipal AuthenticatedUser user) {
		return matchService.getCriteria(user.id());
	}

	@PutMapping("/criteria")
	CriteriaResponse updateCriteria(@AuthenticationPrincipal AuthenticatedUser user,
		@Valid @RequestBody CriteriaRequest request) {
		return matchService.updateCriteria(user.id(), request);
	}
}
