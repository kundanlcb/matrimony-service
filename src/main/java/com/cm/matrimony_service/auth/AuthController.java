package com.cm.matrimony_service.auth;

import com.cm.matrimony_service.auth.AuthDtos.RequestOtpRequest;
import com.cm.matrimony_service.auth.AuthDtos.RequestOtpResponse;
import com.cm.matrimony_service.auth.AuthDtos.VerifyOtpRequest;
import com.cm.matrimony_service.auth.AuthDtos.VerifyOtpResponse;
import com.cm.matrimony_service.auth.AuthDtos.SetupPasswordRequest;
import com.cm.matrimony_service.auth.AuthDtos.LoginRequest;
import com.cm.matrimony_service.auth.AuthDtos.ForgotPasswordRequest;
import com.cm.matrimony_service.auth.AuthDtos.ResetPasswordRequest;
import com.cm.matrimony_service.common.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling authentication and user registration endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * Requests an OTP to be sent to the provided email.
	 * 
	 * @param request containing the email address
	 * @return response containing status and expiration time
	 */
	@PostMapping("/request-otp")
	RequestOtpResponse requestOtp(@Valid @RequestBody RequestOtpRequest request) {
		return authService.requestOtp(request.email());
	}

	/**
	 * Verifies the OTP for a given email.
	 * 
	 * @param request containing email and OTP
	 * @return response containing authentication token and user info
	 */
	@PostMapping("/verify-otp")
	VerifyOtpResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		return authService.verifyOtp(request.email(), request.otp());
	}

	/**
	 * Sets up a password for a newly authenticated user.
	 * 
	 * @param user authenticated user context
	 * @param request containing the new password
	 * @return response containing updated token and user info
	 */
	@PostMapping("/setup-password")
	VerifyOtpResponse setupPassword(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody SetupPasswordRequest request) {
		return authService.setupPassword(user.id(), request.password());
	}

	/**
	 * Authenticates a user with email and password.
	 * 
	 * @param request containing email and password
	 * @return response containing authentication token and user info
	 */
	@PostMapping("/login")
	VerifyOtpResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request.email(), request.password());
	}

	/**
	 * Initiates the password reset process by sending an OTP.
	 * 
	 * @param request containing the email address
	 * @return response containing status and expiration time
	 */
	@PostMapping("/forgot-password")
	RequestOtpResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		return authService.forgotPassword(request.email());
	}

	/**
	 * Resets the user's password using an OTP.
	 * 
	 * @param request containing email, OTP, and new password
	 * @return response containing authentication token and user info
	 */
	@PostMapping("/reset-password")
	VerifyOtpResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		return authService.resetPassword(request.email(), request.otp(), request.newPassword());
	}
}
