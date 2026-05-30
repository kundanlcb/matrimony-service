package com.cm.matrimony_service.user;

import com.cm.matrimony_service.biodata.BiodataDtos.BiodataResponse;
import com.cm.matrimony_service.biodata.BiodataMapper;
import com.cm.matrimony_service.biodata.BiodataRepository;
import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.common.security.AuthenticatedUser;
import com.cm.matrimony_service.interaction.InteractionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserRepository userRepository;
	private final BlockRepository blockRepository;
	private final InteractionRepository interactionRepository;
	private final BiodataRepository biodataRepository;
	private final BiodataMapper biodataMapper;

	@PostMapping("/deactivate")
	@Transactional
	public String deactivate(@AuthenticationPrincipal AuthenticatedUser user) {
		User u = userRepository.findById(user.id())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		u.setActive(false);
		userRepository.save(u);
		return "Account deactivated successfully";
	}

	@PostMapping("/toggle-hidden")
	@Transactional
	public String toggleHidden(@AuthenticationPrincipal AuthenticatedUser user) {
		User u = userRepository.findById(user.id())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		u.setHidden(!u.isHidden());
		userRepository.save(u);
		return u.isHidden() ? "Profile hidden successfully" : "Profile made visible successfully";
	}

	@PostMapping("/delete-request")
	@Transactional
	public String requestDelete(@AuthenticationPrincipal AuthenticatedUser user) {
		User u = userRepository.findById(user.id())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		u.setActive(false);
		userRepository.save(u);
		return "Account deletion scheduled. Your profile is now hidden.";
	}

	@PostMapping("/block/{targetUserId}")
	@Transactional
	public String block(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID targetUserId) {
		if (user.id().equals(targetUserId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot block yourself");
		}
		User blocker = userRepository.findById(user.id())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		User blocked = userRepository.findById(targetUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target user not found"));

		if (blockRepository.findByFromUserIdAndToUserId(user.id(), targetUserId).isPresent()) {
			return "User already blocked";
		}

		blockRepository.save(new Block(blocker, blocked));

		// Withdraw existing interests
		interactionRepository.deleteInteractionsBetween(user.id(), targetUserId);

		return "User blocked successfully";
	}

	@PostMapping("/unblock/{targetUserId}")
	@Transactional
	public String unblock(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID targetUserId) {
		Block block = blockRepository.findByFromUserIdAndToUserId(user.id(), targetUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Blocked relationship not found"));
		blockRepository.delete(block);
		return "User unblocked successfully";
	}

	@GetMapping("/blocked")
	@Transactional(readOnly = true)
	public List<BiodataResponse> getBlocked(@AuthenticationPrincipal AuthenticatedUser user) {
		return blockRepository.findByFromUserId(user.id()).stream()
			.map(Block::getToUser)
			.map(u -> biodataRepository.findByUserId(u.getId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Blocked user biodata not found")))
			.map(biodataMapper::toResponse)
			.toList();
	}
}
