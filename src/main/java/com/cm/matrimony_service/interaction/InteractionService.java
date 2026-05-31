package com.cm.matrimony_service.interaction;

import com.cm.matrimony_service.biodata.BiodataMapper;
import com.cm.matrimony_service.biodata.BiodataRepository;
import com.cm.matrimony_service.biodata.BiodataDtos.BiodataResponse;
import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.interaction.InteractionDtos.SendInteractionResponse;
import com.cm.matrimony_service.user.BlockRepository;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InteractionService {

	private final InteractionRepository interactionRepository;
	private final UserRepository userRepository;
	private final BiodataRepository biodataRepository;
	private final BiodataMapper biodataMapper;
	private final BlockRepository blockRepository;

	@Transactional
	public SendInteractionResponse send(UUID fromUserId, UUID toUserId, String typeValue) {
		if (fromUserId.equals(toUserId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot interact with your own profile");
		}
		InteractionType type = parseType(typeValue);
		User fromUser = userRepository.findById(fromUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		User toUser = userRepository.findById(toUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target user not found"));

		if (blockRepository.existsBlockBetween(fromUserId, toUserId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Action blocked due to account restrictions");
		}

		if (type == InteractionType.INTEREST_SENT) {
			if (!toUser.isActive() || toUser.isHidden()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Profile is currently unavailable");
			}
		}

		boolean mutual = false;
		if (type == InteractionType.INTEREST_SENT) {
			mutual = acceptMutualInterest(fromUserId, toUserId);
			if (mutual) {
				upsert(fromUser, toUser, InteractionType.MATCH_ACCEPTED);
				return new SendInteractionResponse("success", true);
			}
		}
		if (type == InteractionType.MATCH_DECLINED) {
			interactionRepository.findByFromUserIdAndToUserIdAndType(toUserId, fromUserId, InteractionType.INTEREST_SENT)
				.ifPresent(incoming -> {
					incoming.setType(InteractionType.MATCH_DECLINED);
					interactionRepository.save(incoming);
				});
		}

		upsert(fromUser, toUser, type);
		return new SendInteractionResponse("success", mutual);
	}

	@Transactional(readOnly = true)
	public List<BiodataResponse> received(UUID userId) {
		return interactionRepository.findByToUserIdAndType(userId, InteractionType.INTEREST_SENT).stream()
			.map(Interaction::getFromUser)
			.map(user -> biodataRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Sender biodata not found")))
			.map(biodataMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<BiodataResponse> sent(UUID userId) {
		return interactionRepository.findByFromUserIdAndType(userId, InteractionType.INTEREST_SENT).stream()
			.map(Interaction::getToUser)
			.map(user -> biodataRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recipient biodata not found")))
			.map(biodataMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<BiodataResponse> matches(UUID userId) {
		return interactionRepository.findByFromUserIdAndType(userId, InteractionType.MATCH_ACCEPTED).stream()
			.map(Interaction::getToUser)
			.map(user -> biodataRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match biodata not found")))
			.map(biodataMapper::toResponse)
			.toList();
	}

	private boolean acceptMutualInterest(UUID fromUserId, UUID toUserId) {
		return interactionRepository.findByFromUserIdAndToUserIdAndType(toUserId, fromUserId, InteractionType.INTEREST_SENT)
			.map(reverse -> {
				reverse.setType(InteractionType.MATCH_ACCEPTED);
				interactionRepository.save(reverse);
				return true;
			})
			.orElse(false);
	}

	private void upsert(User fromUser, User toUser, InteractionType type) {
		interactionRepository.findByFromUserIdAndToUserIdAndType(fromUser.getId(), toUser.getId(), type)
			.orElseGet(() -> interactionRepository.save(new Interaction(fromUser, toUser, type)));
	}

	private InteractionType parseType(String value) {
		try {
			return InteractionType.valueOf(value.trim().toUpperCase());
		}
		catch (RuntimeException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported interaction type");
		}
	}
}
