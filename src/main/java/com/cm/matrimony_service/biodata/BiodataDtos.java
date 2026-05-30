package com.cm.matrimony_service.biodata;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public final class BiodataDtos {

	private BiodataDtos() {
	}

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
		String phoneNumber,
		String email,
		java.util.List<com.cm.matrimony_service.biodata.AddressDtos.AddressResponse> addresses) {
	}

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
		java.util.List<com.cm.matrimony_service.biodata.AddressDtos.AddressRequest> addresses) {
	}

	public record CompleteRegistrationResponse(String status, String message, String registrationStep) {
	}

	public record MissingFieldsResponse(String status, List<String> missingFields) {
	}
}
