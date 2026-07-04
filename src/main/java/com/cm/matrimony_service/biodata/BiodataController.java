package com.cm.matrimony_service.biodata;

import com.cm.matrimony_service.biodata.BiodataDtos.BiodataResponse;
import com.cm.matrimony_service.biodata.BiodataDtos.UpdateBiodataRequest;
import com.cm.matrimony_service.common.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the authenticated user's biodata.
 */
@RestController
@RequestMapping("/api/v1/biodata/me")
@RequiredArgsConstructor
public class BiodataController {

	private final BiodataService biodataService;

	/**
	 * Retrieves the biodata of the authenticated user.
	 *
	 * @param user the authenticated user
	 * @return the biodata response
	 */
	@GetMapping
	BiodataResponse getMine(@AuthenticationPrincipal AuthenticatedUser user) {
		return biodataService.getMine(user.id());
	}

	/**
	 * Updates the biodata of the authenticated user.
	 *
	 * @param user    the authenticated user
	 * @param request the update request
	 * @return the updated biodata response
	 */
	@PatchMapping
	BiodataResponse updateMine(@AuthenticationPrincipal AuthenticatedUser user,
		@Valid @RequestBody UpdateBiodataRequest request) {
		return biodataService.updateMine(user.id(), request);
	}

	/**
	 * Completes the biodata registration for the authenticated user.
	 *
	 * @param user the authenticated user
	 * @return the response entity indicating status
	 */
	@PostMapping("/complete")
	ResponseEntity<?> complete(@AuthenticationPrincipal AuthenticatedUser user) {
		return biodataService.complete(user.id());
	}
}
