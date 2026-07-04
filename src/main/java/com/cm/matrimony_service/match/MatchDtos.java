package com.cm.matrimony_service.match;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Objects (DTOs) for Match-related operations.
 */
public final class MatchDtos {

	private MatchDtos() {
	}

	/**
	 * Response DTO representing a matched profile.
	 */
	public record MatchProfileResponse(
		UUID id,
		String fullName,
		Integer age,
		String gotra,
		Integer compatibilityScore,
		String photoUrl,
		String profession,
		Long annualIncome,
		String location) {
	}

	/**
	 * Request DTO for updating match criteria.
	 */
	public record CriteriaRequest(
		@Min(18) @Max(70) Integer minAge,
		@Min(18) @Max(70) Integer maxAge,
		@Min(0) Long minIncome,
		String maritalStatus,
		String diet,
		List<String> allowedGotras,
		List<String> allowedLocations,
		List<String> allowedProfessions) {
	}

	/**
	 * Response DTO representing match criteria.
	 */
	public record CriteriaResponse(
		UUID id,
		Integer minAge,
		Integer maxAge,
		Long minIncome,
		String maritalStatus,
		String diet,
		List<String> allowedGotras,
		List<String> allowedLocations,
		List<String> allowedProfessions) {
	}
}
