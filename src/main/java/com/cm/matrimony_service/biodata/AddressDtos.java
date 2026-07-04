package com.cm.matrimony_service.biodata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Data Transfer Objects (DTOs) for Address-related operations.
 */
public final class AddressDtos {

	private AddressDtos() {
	}

	/**
	 * Response DTO for Address.
	 *
	 * @param id            the unique identifier of the address
	 * @param addressType   the type of the address
	 * @param city          the city
	 * @param state         the state
	 * @param country       the country
	 * @param pincode       the postal code
	 * @param streetAddress the street address
	 */
	public record AddressResponse(
		UUID id,
		String addressType,
		String city,
		String state,
		String country,
		String pincode,
		String streetAddress) {
	}

	/**
	 * Request DTO for updating or creating an Address.
	 *
	 * @param addressType   the type of the address
	 * @param city          the city
	 * @param state         the state
	 * @param country       the country
	 * @param pincode       the postal code
	 * @param streetAddress the street address
	 */
	public record AddressRequest(
		@NotBlank String addressType,
		@NotBlank @Size(max = 100) String city,
		@NotBlank @Size(max = 100) String state,
		@NotBlank @Size(max = 100) String country,
		@Size(max = 20) String pincode,
		String streetAddress) {
	}
}
