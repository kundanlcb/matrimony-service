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

/**
 * REST controller for managing user-specific operations such as deactivation,
 * visibility toggle, deletion request, and blocking other users.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserRepository userRepository;
	private final BlockRepository blockRepository;
	private final InteractionRepository interactionRepository;
	private final BiodataRepository biodataRepository;
	private final BiodataMapper biodataMapper;

	/**
	 * Deactivates the authenticated user's account.
	 *
	 * @param user the authenticated user
	 * @return a success message
	 */
	@PostMapping("/deactivate")
	@Transactional
	public String deactivate(@AuthenticationPrincipal AuthenticatedUser user) {
		User u = userRepository.findById(user.id())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		u.setActive(false);
		userRepository.save(u);
		return "Account deactivated successfully";
	}

	/**
	 * Toggles the hidden status of the authenticated user's profile.
	 *
	 * @param user the authenticated user
	 * @return a success message indicating the new visibility status
	 */
	@PostMapping("/toggle-hidden")
	@Transactional
	public String toggleHidden(@AuthenticationPrincipal AuthenticatedUser user) {
		User u = userRepository.findById(user.id())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		u.setHidden(!u.isHidden());
		userRepository.save(u);
		return u.isHidden() ? "Profile hidden successfully" : "Profile made visible successfully";
	}

	/**
	 * Requests the deletion of the authenticated user's account.
	 * The profile will be scheduled for deletion and marked as hidden.
	 *
	 * @param user the authenticated user
	 * @return a success message
	 */
	@PostMapping("/delete-request")
	@Transactional
	public String requestDelete(@AuthenticationPrincipal AuthenticatedUser user) {
		User u = userRepository.findById(user.id())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		u.setActive(false);
		userRepository.save(u);
		return "Account deletion scheduled. Your profile is now hidden.";
	}

	/**
	 * Blocks another user, preventing further interactions and hiding their profile.
	 * Also removes any existing interactions between the two users.
	 *
	 * @param user the authenticated user initiating the block
	 * @param targetUserId the ID of the user to be blocked
	 * @return a success or failure message
	 */
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

	/**
	 * Unblocks a previously blocked user.
	 *
	 * @param user the authenticated user initiating the unblock
	 * @param targetUserId the ID of the user to unblock
	 * @return a success message
	 */
	@PostMapping("/unblock/{targetUserId}")
	@Transactional
	public String unblock(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID targetUserId) {
		Block block = blockRepository.findByFromUserIdAndToUserId(user.id(), targetUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Blocked relationship not found"));
		blockRepository.delete(block);
		return "User unblocked successfully";
	}

	/**
	 * Retrieves the biodata of all users blocked by the authenticated user.
	 *
	 * @param user the authenticated user
	 * @return a list of biodata responses for the blocked users
	 */
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
