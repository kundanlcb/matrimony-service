package com.cm.matrimony_service.match;

import com.cm.matrimony_service.biodata.Biodata;
import com.cm.matrimony_service.biodata.BiodataRepository;
import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.interaction.InteractionRepository;
import com.cm.matrimony_service.interaction.InteractionType;
import com.cm.matrimony_service.match.MatchDtos.CriteriaRequest;
import com.cm.matrimony_service.match.MatchDtos.CriteriaResponse;
import com.cm.matrimony_service.match.MatchDtos.MatchProfileResponse;
import com.cm.matrimony_service.user.BlockRepository;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service for finding matches and managing match criteria.
 */
@Service
@RequiredArgsConstructor
public class MatchService {

	private static final Set<String> SORT_OPTIONS = Set.of("score", "age_asc", "age_desc", "income");

	private final MatchCriteriaRepository criteriaRepository;
	private final BiodataRepository biodataRepository;
	private final UserRepository userRepository;
	private final InteractionRepository interactionRepository;
	private final BlockRepository blockRepository;

	/**
	 * Finds paginated matches for a given user based on their criteria.
	 *
	 * @param userId the user ID
	 * @param page   the page number
	 * @param size   the page size
	 * @param sortBy the field to sort by
	 * @return a page of matched profiles
	 */
	@Transactional(readOnly = true)
	public Page<MatchProfileResponse> findMatches(UUID userId, int page, int size, String sortBy) {
		if (!SORT_OPTIONS.contains(sortBy)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported sortBy value");
		}
		MatchCriteria criteria = getOrCreateCriteria(userId);
		User searcher = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		boolean isSearcherTestUser = searcher.isTestUser();
		
		Set<UUID> excluded = Set.copyOf(interactionRepository.findInteractedUserIds(userId,
			List.of(InteractionType.PASSED, InteractionType.INTEREST_SENT, InteractionType.MATCH_ACCEPTED)));

		List<UUID> blockedUserIds = blockRepository.findBlockedUserIds(userId);

		List<MatchProfileResponse> matches = biodataRepository.findAll().stream()
			.filter(candidate -> !candidate.getUser().getId().equals(userId))
			.filter(candidate -> candidate.getUser().isTestUser() == isSearcherTestUser)
			.filter(candidate -> candidate.getUser().isActive() && !candidate.getUser().isHidden())
			.filter(candidate -> !blockedUserIds.contains(candidate.getUser().getId()))
			.filter(candidate -> !excluded.contains(candidate.getUser().getId()))
			.filter(candidate -> matchesCriteria(candidate, criteria))
			.map(candidate -> toProfile(candidate, criteria))
			.sorted(comparator(sortBy))
			.toList();

		int start = Math.min(page * size, matches.size());
		int end = Math.min(start + size, matches.size());
		return new PageImpl<>(matches.subList(start, end), PageRequest.of(page, size), matches.size());
	}

	/**
	 * Updates the match criteria for a user.
	 *
	 * @param userId  the user ID
	 * @param request the criteria update request
	 * @return the updated criteria response
	 */
	@Transactional
	public CriteriaResponse updateCriteria(UUID userId, CriteriaRequest request) {
		if (request.minAge() != null && request.maxAge() != null && request.minAge() > request.maxAge()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "minAge must be less than or equal to maxAge");
		}
		MatchCriteria criteria = getOrCreateCriteria(userId);
		if (request.minAge() != null) criteria.setMinAge(request.minAge());
		if (request.maxAge() != null) criteria.setMaxAge(request.maxAge());
		if (request.minIncome() != null) criteria.setMinIncome(request.minIncome());
		if (request.maritalStatus() != null) criteria.setMaritalStatus(request.maritalStatus());
		if (request.diet() != null) criteria.setDiet(request.diet());
		if (request.allowedGotras() != null) criteria.setAllowedGotras(new ArrayList<>(request.allowedGotras()));
		if (request.allowedLocations() != null) criteria.setAllowedLocations(new ArrayList<>(request.allowedLocations()));
		if (request.allowedProfessions() != null) criteria.setAllowedProfessions(new ArrayList<>(request.allowedProfessions()));
		return toCriteriaResponse(criteriaRepository.save(criteria));
	}

	/**
	 * Retrieves the match criteria for a user.
	 *
	 * @param userId the user ID
	 * @return the criteria response
	 */
	@Transactional(readOnly = true)
	public CriteriaResponse getCriteria(UUID userId) {
		return toCriteriaResponse(getOrCreateCriteria(userId));
	}

	private MatchCriteria getOrCreateCriteria(UUID userId) {
		return criteriaRepository.findByUserId(userId).orElseGet(() -> {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
			return criteriaRepository.save(new MatchCriteria(user));
		});
	}

	private boolean matchesCriteria(Biodata biodata, MatchCriteria criteria) {
		if (biodata.getAge() == null || biodata.getAge() < criteria.getMinAge() || biodata.getAge() > criteria.getMaxAge()) {
			return false;
		}
		if (biodata.getAnnualIncome() != null && biodata.getAnnualIncome() < criteria.getMinIncome()) {
			return false;
		}
		if (StringUtils.hasText(criteria.getMaritalStatus()) && !criteria.getMaritalStatus().equalsIgnoreCase(biodata.getMaritalStatus())) {
			return false;
		}
		if (StringUtils.hasText(criteria.getDiet()) && !criteria.getDiet().equalsIgnoreCase(biodata.getDiet())) {
			return false;
		}
		return allowed(criteria.getAllowedGotras(), biodata.getGotra())
			&& allowed(criteria.getAllowedLocations(), biodata.getLocation())
			&& allowed(criteria.getAllowedProfessions(), biodata.getProfession());
	}

	private boolean allowed(List<String> allowedValues, String candidateValue) {
		return allowedValues == null || allowedValues.isEmpty() || allowedValues.stream().anyMatch(value -> value.equalsIgnoreCase(candidateValue));
	}

	private MatchProfileResponse toProfile(Biodata biodata, MatchCriteria criteria) {
		return new MatchProfileResponse(
			biodata.getUser().getId(),
			biodata.getFullName(),
			biodata.getAge(),
			biodata.getGotra(),
			compatibilityScore(biodata, criteria),
			biodata.getPhotoUrl(),
			biodata.getProfession(),
			biodata.getAnnualIncome(),
			biodata.getLocation());
	}

	private int compatibilityScore(Biodata biodata, MatchCriteria criteria) {
		int score = 50;
		if (allowed(criteria.getAllowedGotras(), biodata.getGotra())) score += 10;
		if (allowed(criteria.getAllowedLocations(), biodata.getLocation())) score += 10;
		if (allowed(criteria.getAllowedProfessions(), biodata.getProfession())) score += 10;
		if (StringUtils.hasText(criteria.getDiet()) && criteria.getDiet().equalsIgnoreCase(biodata.getDiet())) score += 10;
		if (StringUtils.hasText(criteria.getMaritalStatus()) && criteria.getMaritalStatus().equalsIgnoreCase(biodata.getMaritalStatus())) score += 10;
		return Math.min(score, 100);
	}

	private Comparator<MatchProfileResponse> comparator(String sortBy) {
		return switch (sortBy) {
			case "age_asc" -> Comparator.comparing(MatchProfileResponse::age, Comparator.nullsLast(Integer::compareTo));
			case "age_desc" -> Comparator.comparing(MatchProfileResponse::age, Comparator.nullsLast(Integer::compareTo)).reversed();
			case "income" -> Comparator.comparing(MatchProfileResponse::annualIncome, Comparator.nullsLast(Long::compareTo)).reversed();
			default -> Comparator.comparing(MatchProfileResponse::compatibilityScore).reversed();
		};
	}

	private CriteriaResponse toCriteriaResponse(MatchCriteria criteria) {
		return new CriteriaResponse(criteria.getId(), criteria.getMinAge(), criteria.getMaxAge(), criteria.getMinIncome(),
			criteria.getMaritalStatus(), criteria.getDiet(), List.copyOf(criteria.getAllowedGotras()),
			List.copyOf(criteria.getAllowedLocations()), List.copyOf(criteria.getAllowedProfessions()));
	}
}
