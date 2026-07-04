package com.cm.matrimony_service.auth;

import com.cm.matrimony_service.config.AppProperties;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for admin authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAuthController {

	private final AppProperties appProperties;
	private final JwtService jwtService;

	/**
	 * Authenticates an admin user and returns a JWT token.
	 * 
	 * @param request map containing username and password
	 * @return ResponseEntity containing the token or an error message
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
		String username = request.get("username");
		String password = request.get("password");

		AppProperties.Admin adminConfig = appProperties.admin();
		if (adminConfig != null && adminConfig.username().equals(username) && adminConfig.password().equals(password)) {
			// Issue JWT with ROLE_ADMIN. We use a zeroed UUID for the admin user ID.
			UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000000");
			String token = jwtService.issueToken(adminId, username, "ADMIN");
			return ResponseEntity.ok(Map.of("token", token));
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid admin credentials"));
	}
}
