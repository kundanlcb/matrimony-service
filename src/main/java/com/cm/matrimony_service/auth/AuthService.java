package com.cm.matrimony_service.auth;

import com.cm.matrimony_service.auth.AuthDtos.AuthUserResponse;
import com.cm.matrimony_service.auth.AuthDtos.RequestOtpResponse;
import com.cm.matrimony_service.auth.AuthDtos.VerifyOtpResponse;
import com.cm.matrimony_service.biodata.Biodata;
import com.cm.matrimony_service.biodata.BiodataRepository;
import com.cm.matrimony_service.match.MatchCriteria;
import com.cm.matrimony_service.match.MatchCriteriaRepository;
import com.cm.matrimony_service.user.RegistrationStep;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import com.cm.matrimony_service.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final OtpService otpService;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final BiodataRepository biodataRepository;
	private final MatchCriteriaRepository criteriaRepository;
	private final PasswordEncoder passwordEncoder;

	public RequestOtpResponse requestOtp(String mobileNumber) {
		int expiresInSeconds = otpService.request(mobileNumber);
		return new RequestOtpResponse("success", "OTP sent successfully", expiresInSeconds);
	}

	@Transactional
	public VerifyOtpResponse verifyOtp(String mobileNumber, String otp) {
		otpService.verify(mobileNumber, otp);
		User user = userRepository.findByMobileNumber(mobileNumber).orElseGet(() -> new User(mobileNumber));
		user.setVerified(true);
		if (user.getRegistrationStep() == RegistrationStep.AUTH) {
			user.setRegistrationStep(RegistrationStep.PASSWORD);
		}
		User savedUser = userRepository.save(user);

		String token = jwtService.issueToken(savedUser.getId(), savedUser.getMobileNumber(), "USER");
		AuthUserResponse responseUser = new AuthUserResponse(savedUser.getId(), savedUser.getMobileNumber(),
			savedUser.getRegistrationStep().name().toLowerCase(), savedUser.getPreferredLanguage().name().toLowerCase());
		return new VerifyOtpResponse("success", token, responseUser);
	}

	@Transactional
	public VerifyOtpResponse setupPassword(UUID userId, String password) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
			
		user.setPassword(passwordEncoder.encode(password));
		
		if (user.getRegistrationStep() == RegistrationStep.PASSWORD) {
			user.setRegistrationStep(RegistrationStep.BIODATA);
		}
		
		User savedUser = userRepository.save(user);
		biodataRepository.findByUserId(savedUser.getId()).orElseGet(() -> biodataRepository.save(new Biodata(savedUser)));
		criteriaRepository.findByUserId(savedUser.getId()).orElseGet(() -> criteriaRepository.save(new MatchCriteria(savedUser)));

		String token = jwtService.issueToken(savedUser.getId(), savedUser.getMobileNumber(), "USER");
		AuthUserResponse responseUser = new AuthUserResponse(savedUser.getId(), savedUser.getMobileNumber(),
			savedUser.getRegistrationStep().name().toLowerCase(), savedUser.getPreferredLanguage().name().toLowerCase());
		return new VerifyOtpResponse("success", token, responseUser);
	}

	public VerifyOtpResponse login(String mobileNumber, String password) {
		User user = userRepository.findByMobileNumber(mobileNumber)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid mobile number or password"));
			
		if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid mobile number or password");
		}
		
		String token = jwtService.issueToken(user.getId(), user.getMobileNumber(), "USER");
		AuthUserResponse responseUser = new AuthUserResponse(user.getId(), user.getMobileNumber(),
			user.getRegistrationStep().name().toLowerCase(), user.getPreferredLanguage().name().toLowerCase());
		return new VerifyOtpResponse("success", token, responseUser);
	}
}
