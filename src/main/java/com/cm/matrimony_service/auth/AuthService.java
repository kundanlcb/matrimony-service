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

/**
 * Service class handling authentication logic including OTP, login, and password management.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final OtpService otpService;
	private final EmailService emailService;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final BiodataRepository biodataRepository;
	private final MatchCriteriaRepository criteriaRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * Requests an OTP for the given email and sends it via email.
	 * 
	 * @param email the email address
	 * @return response containing the OTP expiration time
	 */
	public RequestOtpResponse requestOtp(String email) {
		int expiresInSeconds = otpService.request(email);
		emailService.sendOtpEmail(email, otpService.getLatestCode(email));
		return new RequestOtpResponse("success", "OTP sent successfully to email", expiresInSeconds);
	}

	/**
	 * Verifies the provided OTP for the given email and registers or logs in the user.
	 * 
	 * @param email the email address
	 * @param otp the OTP code
	 * @return response containing JWT token and user info
	 */
	@Transactional
	public VerifyOtpResponse verifyOtp(String email, String otp) {
		otpService.verify(email, otp);
		User user = userRepository.findByEmail(email).orElseGet(() -> new User(email));
		user.setVerified(true);
		if (user.getRegistrationStep() == RegistrationStep.AUTH) {
			user.setRegistrationStep(RegistrationStep.PASSWORD);
		}
		User savedUser = userRepository.save(user);

		String token = jwtService.issueToken(savedUser.getId(), email, "USER");
		AuthUserResponse responseUser = new AuthUserResponse(savedUser.getId(), savedUser.getEmail(),
			savedUser.getRegistrationStep().name().toLowerCase(), savedUser.getPreferredLanguage().name().toLowerCase());
		return new VerifyOtpResponse("success", token, responseUser);
	}

	/**
	 * Sets up a new password for the user and progresses their registration step.
	 * 
	 * @param userId the ID of the user
	 * @param password the new password
	 * @return response containing JWT token and user info
	 */
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

		String token = jwtService.issueToken(savedUser.getId(), savedUser.getEmail(), "USER");
		AuthUserResponse responseUser = new AuthUserResponse(savedUser.getId(), savedUser.getEmail(),
			savedUser.getRegistrationStep().name().toLowerCase(), savedUser.getPreferredLanguage().name().toLowerCase());
		return new VerifyOtpResponse("success", token, responseUser);
	}

	/**
	 * Authenticates the user using email and password.
	 * 
	 * @param email the email address
	 * @param password the user password
	 * @return response containing JWT token and user info
	 */
	public VerifyOtpResponse login(String email, String password) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
			
		if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		
		String token = jwtService.issueToken(user.getId(), user.getEmail(), "USER");
		AuthUserResponse responseUser = new AuthUserResponse(user.getId(), user.getEmail(),
			user.getRegistrationStep().name().toLowerCase(), user.getPreferredLanguage().name().toLowerCase());
		return new VerifyOtpResponse("success", token, responseUser);
	}

	/**
	 * Initiates the forgot password process by sending an OTP to a registered user.
	 * 
	 * @param email the email address
	 * @return response containing the OTP expiration time
	 */
	public RequestOtpResponse forgotPassword(String email) {
		userRepository.findByEmail(email)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User with this email does not exist"));
		
		int expiresInSeconds = otpService.request(email);
		emailService.sendOtpEmail(email, otpService.getLatestCode(email));
		return new RequestOtpResponse("success", "Password reset OTP sent successfully to email", expiresInSeconds);
	}

	/**
	 * Resets the user's password using the verified OTP.
	 * 
	 * @param email the email address
	 * @param otp the OTP code
	 * @param newPassword the new password
	 * @return response containing JWT token and user info
	 */
	@Transactional
	public VerifyOtpResponse resetPassword(String email, String otp, String newPassword) {
		otpService.verify(email, otp);
		
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
			
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		String token = jwtService.issueToken(user.getId(), user.getEmail(), "USER");
		AuthUserResponse responseUser = new AuthUserResponse(user.getId(), user.getEmail(),
			user.getRegistrationStep().name().toLowerCase(), user.getPreferredLanguage().name().toLowerCase());
		return new VerifyOtpResponse("success", token, responseUser);
	}
}
