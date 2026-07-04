package com.cm.matrimony_service.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

/**
 * Data Transfer Objects for authentication operations.
 */
public final class AuthDtos {

	private AuthDtos() {
	}

	public record RequestOtpRequest(
		@NotBlank @Email String email) {
	}

	public record RequestOtpResponse(String status, String message, int expiresInSeconds) {
	}

	public record VerifyOtpRequest(
		@NotBlank @Email String email,
		@NotBlank @Pattern(regexp = "^\\d{6}$") String otp) {
	}

	public record AuthUserResponse(UUID id, String email, String registrationStep, String preferredLanguage) {
	}

	public record VerifyOtpResponse(String status, String token, AuthUserResponse user) {
	}

	public record SetupPasswordRequest(
		@NotBlank String password) {
	}

	public record LoginRequest(
		@NotBlank @Email String email,
		@NotBlank String password) {
	}

	public record ForgotPasswordRequest(
		@NotBlank @Email String email) {
	}

	public record ResetPasswordRequest(
		@NotBlank @Email String email,
		@NotBlank @Pattern(regexp = "^\\d{6}$") String otp,
		@NotBlank String newPassword) {
	}
}
