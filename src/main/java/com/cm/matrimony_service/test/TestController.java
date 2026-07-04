package com.cm.matrimony_service.test;

import com.cm.matrimony_service.biodata.BiodataRepository;
import com.cm.matrimony_service.match.MatchCriteriaRepository;
import com.cm.matrimony_service.interaction.InteractionRepository;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Controller providing endpoints for testing and automation purposes, such as e2e testing.
 */
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

	private final UserRepository userRepository;
	private final BiodataRepository biodataRepository;
	private final MatchCriteriaRepository matchCriteriaRepository;
	private final InteractionRepository interactionRepository;

	/**
	 * Tears down the test data generated during e2e testing.
	 * 
	 * @param secret the secret key required to execute the teardown
	 * @return ResponseEntity containing a success message or an error if unauthorized
	 */
	@PostMapping("/teardown")
	@Transactional
	public ResponseEntity<String> teardown(@RequestHeader(value = "X-Test-Secret", required = false) String secret) {
		if (!"mithila-e2e-secret-key-2026".equals(secret)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid secret");
		}
		List<User> testUsers = userRepository.findByTestUserTrue();
		for (User user : testUsers) {
			interactionRepository.deleteAllByUserId(user.getId());
			biodataRepository.findByUserId(user.getId()).ifPresent(biodataRepository::delete);
			matchCriteriaRepository.findByUserId(user.getId()).ifPresent(matchCriteriaRepository::delete);
		}
		userRepository.deleteAll(testUsers);
		return ResponseEntity.ok("Teardown completed. Deleted " + testUsers.size() + " test users.");
	}
}
