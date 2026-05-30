package com.cm.matrimony_service.biodata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class AddressDtos {

	private AddressDtos() {
	}

	public record AddressResponse(
		UUID id,
		String addressType,
		String city,
		String state,
		String country,
		String pincode,
		String streetAddress) {
	}

	public record AddressRequest(
		@NotBlank String addressType,
		@NotBlank @Size(max = 100) String city,
		@NotBlank @Size(max = 100) String state,
		@NotBlank @Size(max = 100) String country,
		@Size(max = 20) String pincode,
		String streetAddress) {
	}
}
