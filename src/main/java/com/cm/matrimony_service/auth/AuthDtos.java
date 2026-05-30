package com.cm.matrimony_service.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record RequestOtpRequest(
		@NotBlank @Pattern(regexp = "^(?:\\+[1-9]\\d{7,14}|[1-9]\\d{9})$") String mobileNumber) {
	}

	public record RequestOtpResponse(String status, String message, int expiresInSeconds) {
	}

	public record VerifyOtpRequest(
		@NotBlank @Pattern(regexp = "^(?:\\+[1-9]\\d{7,14}|[1-9]\\d{9})$") String mobileNumber,
		@NotBlank @Pattern(regexp = "^\\d{6}$") String otp) {
	}

	public record AuthUserResponse(UUID id, String mobileNumber, String registrationStep, String preferredLanguage) {
	}

	public record VerifyOtpResponse(String status, String token, AuthUserResponse user) {
	}

	public record SetupPasswordRequest(
		@NotBlank String password) {
	}

	public record LoginRequest(
		@NotBlank @Pattern(regexp = "^(?:\\+[1-9]\\d{7,14}|[1-9]\\d{9})$") String mobileNumber,
		@NotBlank String password) {
	}
}
