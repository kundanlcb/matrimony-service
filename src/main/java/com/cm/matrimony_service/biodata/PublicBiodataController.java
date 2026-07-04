package com.cm.matrimony_service.biodata;

import com.cm.matrimony_service.biodata.BiodataDtos.PublicBiodataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

/**
 * REST controller for accessing public biodata profiles.
 */
@RestController
@RequestMapping("/api/v1/biodata/public")
@RequiredArgsConstructor
public class PublicBiodataController {

	private final BiodataService biodataService;

	/**
	 * Retrieves the public profile of a user by their user ID.
	 *
	 * @param userId the unique identifier of the user
	 * @return the public biodata response
	 */
	@GetMapping("/{userId}")
	public PublicBiodataResponse getPublicProfile(@PathVariable UUID userId) {
		return biodataService.getPublicBiodata(userId);
	}
}
