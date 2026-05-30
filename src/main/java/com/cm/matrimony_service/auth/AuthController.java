package com.cm.matrimony_service.auth;

import com.cm.matrimony_service.auth.AuthDtos.RequestOtpRequest;
import com.cm.matrimony_service.auth.AuthDtos.RequestOtpResponse;
import com.cm.matrimony_service.auth.AuthDtos.VerifyOtpRequest;
import com.cm.matrimony_service.auth.AuthDtos.VerifyOtpResponse;
import com.cm.matrimony_service.auth.AuthDtos.SetupPasswordRequest;
import com.cm.matrimony_service.auth.AuthDtos.LoginRequest;
import com.cm.matrimony_service.common.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/request-otp")
	RequestOtpResponse requestOtp(@Valid @RequestBody RequestOtpRequest request) {
		return authService.requestOtp(request.mobileNumber());
	}

	@PostMapping("/verify-otp")
	VerifyOtpResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		return authService.verifyOtp(request.mobileNumber(), request.otp());
	}

	@PostMapping("/setup-password")
	VerifyOtpResponse setupPassword(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody SetupPasswordRequest request) {
		return authService.setupPassword(user.id(), request.password());
	}

	@PostMapping("/login")
	VerifyOtpResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request.mobileNumber(), request.password());
	}
}
