package com.cm.matrimony_service.auth;

import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.config.AppProperties;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

	private final AppProperties properties;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();
	private final Map<String, OtpChallenge> challenges = new ConcurrentHashMap<>();

	public OtpService(AppProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
	}

	public int request(String email) {
		String code = configuredCode(email);
		challenges.put(email, new OtpChallenge(code, clock.instant().plusSeconds(properties.otp().expiresInSeconds())));
		return properties.otp().expiresInSeconds();
	}

	public void verify(String email, String otp) {
		OtpChallenge challenge = challenges.get(email);
		if (challenge == null || challenge.expiresAt().isBefore(clock.instant()) || !challenge.code().equals(otp)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
		}
		challenges.remove(email);
	}

	public String getLatestCode(String email) {
		OtpChallenge challenge = challenges.get(email);
		return challenge != null ? challenge.code() : null;
	}

	private String configuredCode(String email) {
		if (email != null && email.startsWith("test")) {
			return "123456";
		}
		if (properties.otp().fixedCode() != null && !properties.otp().fixedCode().isBlank()) {
			return properties.otp().fixedCode();
		}
		return String.format("%06d", random.nextInt(1_000_000));
	}

	private record OtpChallenge(String code, Instant expiresAt) {
	}
}
