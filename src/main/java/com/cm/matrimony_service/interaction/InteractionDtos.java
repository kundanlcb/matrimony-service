package com.cm.matrimony_service.interaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public final class InteractionDtos {

	private InteractionDtos() {
	}

	public record SendInteractionRequest(@NotNull UUID toUserId, @NotBlank String type) {
	}

	public record SendInteractionResponse(String status, boolean isMutualMatch) {
	}
}
