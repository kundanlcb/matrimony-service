package com.cm.matrimony_service.auth;

import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.common.security.AuthenticatedUser;
import com.cm.matrimony_service.config.AppProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service class for issuing and parsing JSON Web Tokens (JWT).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<Map<String, Object>> CLAIMS = new TypeReference<>() {
	};

	private final AppProperties properties;
	private final Clock clock;

	/**
	 * Issues a new JWT token for a given user.
	 * 
	 * @param userId the user's ID
	 * @param email the user's email
	 * @param role the user's role
	 * @return the encoded JWT token string
	 */
	public String issueToken(UUID userId, String email, String role) {
		Instant now = clock.instant();
		Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", userId.toString());
		payload.put("email", email);
		payload.put("role", role != null ? role : "USER");
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", now.plusSeconds(properties.jwt().expirationSeconds()).getEpochSecond());

		String unsigned = encodeJson(header) + "." + encodeJson(payload);
		return unsigned + "." + sign(unsigned);
	}

	/**
	 * Parses and validates a given JWT token.
	 * 
	 * @param token the encoded JWT token string
	 * @return the authenticated user details extracted from the token
	 */
	public AuthenticatedUser parse(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw unauthorized("Invalid token");
		}
		String unsigned = parts[0] + "." + parts[1];
		if (!constantTimeEquals(sign(unsigned), parts[2])) {
			throw unauthorized("Invalid token signature");
		}
		Map<String, Object> claims = decodeClaims(parts[1]);
		long expiresAt = ((Number) claims.get("exp")).longValue();
		if (clock.instant().getEpochSecond() >= expiresAt) {
			throw unauthorized("Token expired");
		}
		String role = (String) claims.get("role");
		if (role == null) role = "USER";
		return new AuthenticatedUser(UUID.fromString((String) claims.get("sub")), (String) claims.get("email"), role);
	}

	private String encodeJson(Map<String, Object> content) {
		try {
			return Base64.getUrlEncoder().withoutPadding().encodeToString(OBJECT_MAPPER.writeValueAsBytes(content));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to encode token", ex);
		}
	}

	private Map<String, Object> decodeClaims(String encodedClaims) {
		try {
			byte[] decoded = Base64.getUrlDecoder().decode(encodedClaims);
			return OBJECT_MAPPER.readValue(decoded, CLAIMS);
		}
		catch (Exception ex) {
			throw unauthorized("Invalid token payload");
		}
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(properties.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to sign token", ex);
		}
	}

	private boolean constantTimeEquals(String expected, String actual) {
		return MessageDigestSupport.equals(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
	}

	private ApiException unauthorized(String message) {
		return new ApiException(HttpStatus.UNAUTHORIZED, message);
	}
}
