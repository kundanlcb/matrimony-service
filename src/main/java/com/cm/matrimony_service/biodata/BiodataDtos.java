package com.cm.matrimony_service.biodata;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Objects (DTOs) for Biodata-related operations.
 */
public final class BiodataDtos {

	private BiodataDtos() {
	}

	/**
	 * Response DTO representing a user's biodata.
	 */
	public record BiodataResponse(
		UUID id,
		String fullName,
		String gender,
		Integer age,
		String gotra,
		String religion,
		String caste,
		String profession,
		Long annualIncome,
		String location,
		String education,
		String aboutMe,
		String photoUrl,
		String height,
		String maritalStatus,
		String diet,
		String complexion,
		List<String> interests,
		List<String> additionalPhotos,
		String email,
		List<AddressDtos.AddressResponse> addresses) {
	}

	/**
	 * Request DTO for updating a user's biodata.
	 */
	public record UpdateBiodataRequest(
		@Size(max = 100) String fullName,
		String gender,
		@Min(18) @Max(70) Integer age,
		@Size(max = 50) String gotra,
		@Size(max = 50) String religion,
		@Size(max = 100) String caste,
		@Size(max = 100) String profession,
		Long annualIncome,
		@Size(max = 100) String location,
		@Size(max = 255) String education,
		String aboutMe,
		@Size(max = 512) String photoUrl,
		@Size(max = 20) String height,
		@Size(max = 30) String maritalStatus,
		@Size(max = 30) String diet,
		@Size(max = 30) String complexion,
		List<String> interests,
		List<String> additionalPhotos,
		String email,
		List<AddressDtos.AddressRequest> addresses) {
	}

	/**
	 * Response DTO for completing registration.
	 */
	public record CompleteRegistrationResponse(String status, String message, String registrationStep) {
	}

	/**
	 * Response DTO for public biodata view.
	 */
	public record PublicBiodataResponse(
		UUID id,
		String fullName,
		String gender,
		Integer age,
		String height,
		String location,
		String gotra,
		String religion,
		String caste,
		String photoUrl,
		String profession,
		String education,
		String aboutMe) {
	}

	/**
	 * Response DTO for missing fields during registration.
	 */
	public record MissingFieldsResponse(String status, List<String> missingFields) {
	}
}
