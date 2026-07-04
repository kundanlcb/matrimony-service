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

/**
 * REST controller for managing match profiles and criteria.
 */
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

	private final MatchService matchService;

	/**
	 * Finds matching profiles for the authenticated user.
	 *
	 * @param user   the authenticated user
	 * @param page   the page number
	 * @param size   the page size
	 * @param sortBy the field to sort by
	 * @return a page of matching profiles
	 */
	@GetMapping
	Page<MatchProfileResponse> findMatches(@AuthenticationPrincipal AuthenticatedUser user,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "score") String sortBy) {
		return matchService.findMatches(user.id(), page, size, sortBy);
	}

	/**
	 * Retrieves the match criteria for the authenticated user.
	 *
	 * @param user the authenticated user
	 * @return the match criteria response
	 */
	@GetMapping("/criteria")
	CriteriaResponse getCriteria(@AuthenticationPrincipal AuthenticatedUser user) {
		return matchService.getCriteria(user.id());
	}

	/**
	 * Updates the match criteria for the authenticated user.
	 *
	 * @param user    the authenticated user
	 * @param request the update request
	 * @return the updated match criteria response
	 */
	@PutMapping("/criteria")
	CriteriaResponse updateCriteria(@AuthenticationPrincipal AuthenticatedUser user,
		@Valid @RequestBody CriteriaRequest request) {
		return matchService.updateCriteria(user.id(), request);
	}
}
