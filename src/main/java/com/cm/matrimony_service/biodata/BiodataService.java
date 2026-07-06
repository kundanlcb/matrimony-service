package com.cm.matrimony_service.biodata;

import com.cm.matrimony_service.biodata.BiodataDtos.BiodataResponse;
import com.cm.matrimony_service.biodata.BiodataDtos.CompleteRegistrationResponse;
import com.cm.matrimony_service.biodata.BiodataDtos.MissingFieldsResponse;
import com.cm.matrimony_service.biodata.BiodataDtos.UpdateBiodataRequest;
import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.user.RegistrationStep;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import com.cm.matrimony_service.auth.EmailService;
import com.cm.matrimony_service.biodata.BiodataDtos.PublicBiodataResponse;
import com.cm.matrimony_service.biodata.AddressDtos.AddressRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service for managing user biodata.
 */
@Service
@RequiredArgsConstructor
public class BiodataService {

	private final BiodataRepository biodataRepository;
	private final UserRepository userRepository;
	private final BiodataMapper mapper;
	private final EmailService emailService;

	/**
	 * Gets the biodata for the specified user, creating it if it doesn't exist.
	 *
	 * @param userId the user ID
	 * @return the biodata response
	 */
	@Transactional(readOnly = true)
	public BiodataResponse getMine(UUID userId) {
		return mapper.toResponse(getOrCreateByUserId(userId));
	}

	/**
	 * Retrieves the public biodata profile for a user.
	 *
	 * @param userId the user ID
	 * @return the public biodata response
	 */
	@Transactional(readOnly = true)
	public PublicBiodataResponse getPublicBiodata(UUID userId) {
		Biodata biodata = biodataRepository.findByUserId(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile not found"));
		return new PublicBiodataResponse(
			biodata.getId(),
			biodata.getFullName(),
			biodata.getGender() != null ? biodata.getGender().name() : null,
			biodata.getAge(),
			biodata.getHeight(),
			biodata.getLocation(),
			biodata.getGotra(),
			biodata.getMool(),
			biodata.getReligion(),
			biodata.getCaste(),
			biodata.getPhotoUrl(),
			biodata.getProfession(),
			biodata.getEducation(),
			biodata.getAboutMe()
		);
	}

	/**
	 * Updates the biodata for the specified user.
	 *
	 * @param userId  the user ID
	 * @param request the update request
	 * @return the updated biodata response
	 */
	@Transactional
	public BiodataResponse updateMine(UUID userId, UpdateBiodataRequest request) {
		Biodata biodata = getOrCreateByUserId(userId);
		applyPatch(biodata, request);
		return mapper.toResponse(biodataRepository.save(biodata));
	}

	/**
	 * Completes the registration process for the user's biodata.
	 *
	 * @param userId the user ID
	 * @return a response entity indicating success or validation errors
	 */
	@Transactional
	public ResponseEntity<?> complete(UUID userId) {
		Biodata biodata = getOrCreateByUserId(userId);
		List<String> missing = missingRequiredFields(biodata);
		if (!missing.isEmpty()) {
			return ResponseEntity.badRequest().body(new MissingFieldsResponse("error", missing));
		}
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		user.setRegistrationStep(RegistrationStep.COMPLETED);
		userRepository.save(user);
		
		if (user.getEmail() != null) {
			emailService.sendRegistrationCompleteEmail(user.getEmail(), biodata);
		}
		
		return ResponseEntity.ok(new CompleteRegistrationResponse("success", "Registration completed", "completed"));
	}

	private Biodata getOrCreateByUserId(UUID userId) {
		return biodataRepository.findByUserId(userId)
			.orElseGet(() -> {
				User user = userRepository.findById(userId)
					.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
				return biodataRepository.save(new Biodata(user));
			});
	}

	private void applyPatch(Biodata biodata, UpdateBiodataRequest request) {
		if (request.fullName() != null) biodata.setFullName(request.fullName());
		if (request.gender() != null) biodata.setGender(parseGender(request.gender()));
		if (request.age() != null) biodata.setAge(request.age());
		if (request.gotra() != null) biodata.setGotra(request.gotra());
		if (request.mool() != null) biodata.setMool(request.mool());
		if (request.dateOfBirth() != null) biodata.setDateOfBirth(request.dateOfBirth());
		if (request.birthTime() != null) biodata.setBirthTime(request.birthTime());
		if (request.birthPlace() != null) biodata.setBirthPlace(request.birthPlace());
		if (request.fatherName() != null) biodata.setFatherName(request.fatherName());
		if (request.motherName() != null) biodata.setMotherName(request.motherName());
		if (request.siblingsDetail() != null) biodata.setSiblingsDetail(request.siblingsDetail());
		if (request.grandparentName() != null) biodata.setGrandparentName(request.grandparentName());
		if (request.religion() != null) biodata.setReligion(request.religion());
		if (request.caste() != null) biodata.setCaste(request.caste());
		if (request.profession() != null) biodata.setProfession(request.profession());
		if (request.annualIncome() != null) biodata.setAnnualIncome(request.annualIncome());
		if (request.location() != null) biodata.setLocation(request.location());
		if (request.education() != null) biodata.setEducation(request.education());
		if (request.aboutMe() != null) biodata.setAboutMe(request.aboutMe());
		if (request.photoUrl() != null) biodata.setPhotoUrl(request.photoUrl());
		if (request.height() != null) biodata.setHeight(request.height());
		if (request.maritalStatus() != null) biodata.setMaritalStatus(request.maritalStatus());
		if (request.diet() != null) biodata.setDiet(request.diet());
		if (request.complexion() != null) biodata.setComplexion(request.complexion());
		if (request.interests() != null) biodata.setInterests(new ArrayList<>(request.interests()));
		if (request.additionalPhotos() != null) biodata.setAdditionalPhotos(new ArrayList<>(request.additionalPhotos()));
		
		if (request.phoneNumber() != null) biodata.setPhoneNumber(request.phoneNumber());

		if (request.email() != null) {
			biodata.getUser().setEmail(request.email());
			userRepository.save(biodata.getUser());
		}

		if (request.addresses() != null) {
			for (AddressRequest addrReq : request.addresses()) {
				if (addrReq.addressType() == null) continue;
				AddressType type;
				try {
					type = AddressType.valueOf(addrReq.addressType().trim().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid address type: " + addrReq.addressType());
				}

				Address existing = biodata.getAddresses().stream()
					.filter(a -> a.getAddressType() == type)
					.findFirst()
					.orElse(null);

				if (existing != null) {
					if (addrReq.city() != null) existing.setCity(addrReq.city());
					if (addrReq.state() != null) existing.setState(addrReq.state());
					if (addrReq.country() != null) existing.setCountry(addrReq.country());
					if (addrReq.pincode() != null) existing.setPincode(addrReq.pincode());
					if (addrReq.streetAddress() != null) existing.setStreetAddress(addrReq.streetAddress());
					existing.setPrimary(type == AddressType.CURRENT);
				} else {
					Address newAddr = new Address(
						biodata,
						type,
						addrReq.city() != null ? addrReq.city() : "N/A",
						addrReq.state() != null ? addrReq.state() : "N/A",
						addrReq.country() != null ? addrReq.country() : "India",
						addrReq.pincode(),
						addrReq.streetAddress(),
						type == AddressType.CURRENT
					);
					biodata.getAddresses().add(newAddr);
				}
			}
		}
	}

	private Gender parseGender(String value) {
		try {
			return Gender.valueOf(value.trim().toUpperCase());
		}
		catch (RuntimeException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "gender must be Male or Female");
		}
	}

	private List<String> missingRequiredFields(Biodata biodata) {
		List<String> missing = new ArrayList<>();
		if (!StringUtils.hasText(biodata.getFullName())) missing.add("fullName");
		if (biodata.getGender() == null) missing.add("gender");
		if (!StringUtils.hasText(biodata.getDateOfBirth())) missing.add("dateOfBirth");
		if (!StringUtils.hasText(biodata.getGotra())) missing.add("gotra");
		if (!StringUtils.hasText(biodata.getProfession())) missing.add("profession");
		if (biodata.getAnnualIncome() == null) missing.add("annualIncome");
		if (!StringUtils.hasText(biodata.getLocation())) missing.add("location");
		if (!StringUtils.hasText(biodata.getEducation())) missing.add("education");
		if (!StringUtils.hasText(biodata.getPhotoUrl())) missing.add("photoUrl");

		boolean hasCurrentAddress = biodata.getAddresses().stream()
			.anyMatch(a -> a.getAddressType() == AddressType.CURRENT);
		if (!hasCurrentAddress) {
			missing.add("currentAddress");
		}

		return missing;
	}
}
