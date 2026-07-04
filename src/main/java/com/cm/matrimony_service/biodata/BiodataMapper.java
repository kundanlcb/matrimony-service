package com.cm.matrimony_service.biodata;

import com.cm.matrimony_service.biodata.BiodataDtos.BiodataResponse;
import com.cm.matrimony_service.common.security.AuthenticatedUser;
import com.cm.matrimony_service.subscription.SubscriptionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Biodata entities and DTOs.
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class BiodataMapper {

	private final SubscriptionService subscriptionService;

	public BiodataResponse toResponse(Biodata biodata) {
		UUID viewerId = getCurrentUserId();
		UUID targetUserId = biodata.getUser().getId();
		boolean unlocked = viewerId != null && subscriptionService.isUnlocked(viewerId, targetUserId);


		String email = unlocked ? biodata.getUser().getEmail() : null;
		String phoneNumber = unlocked ? biodata.getPhoneNumber() : null;
		List<AddressDtos.AddressResponse> mappedAddresses = mapAddresses(biodata.getAddresses(), unlocked);

		return new BiodataResponse(
			biodata.getId(),
			biodata.getFullName(),
			toDisplayName(biodata.getGender()),
			biodata.getAge(),
			biodata.getGotra(),
			biodata.getReligion(),
			biodata.getCaste(),
			biodata.getProfession(),
			biodata.getAnnualIncome(),
			biodata.getLocation(),
			biodata.getEducation(),
			biodata.getAboutMe(),
			biodata.getPhotoUrl(),
			biodata.getHeight(),
			biodata.getMaritalStatus(),
			biodata.getDiet(),
			biodata.getComplexion(),
			biodata.getInterests() == null ? List.of() : List.copyOf(biodata.getInterests()),
			biodata.getAdditionalPhotos() == null ? List.of() : List.copyOf(biodata.getAdditionalPhotos()),
			email,
			phoneNumber,
			mappedAddresses);
	}

	private String toDisplayName(Gender gender) {
		if (gender == null) {
			return null;
		}
		return gender.name().charAt(0) + gender.name().substring(1).toLowerCase();
	}

	private UUID getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser) {
			return ((AuthenticatedUser) auth.getPrincipal()).id();
		}
		return null;
	}

	private List<AddressDtos.AddressResponse> mapAddresses(List<Address> addresses, boolean unlocked) {
		if (addresses == null) {
			return List.of();
		}
		return addresses.stream()
			.map(addr -> new AddressDtos.AddressResponse(
				addr.getId(),
				addr.getAddressType().name().toLowerCase(),
				addr.getCity(),
				addr.getState(),
				addr.getCountry(),
				unlocked ? addr.getPincode() : null,
				unlocked ? addr.getStreetAddress() : null
			))
			.toList();
	}
}
