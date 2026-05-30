package com.cm.matrimony_service.biodata;

import com.cm.matrimony_service.biodata.BiodataDtos.BiodataResponse;
import com.cm.matrimony_service.biodata.BiodataDtos.CompleteRegistrationResponse;
import com.cm.matrimony_service.biodata.BiodataDtos.MissingFieldsResponse;
import com.cm.matrimony_service.biodata.BiodataDtos.UpdateBiodataRequest;
import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.user.RegistrationStep;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BiodataService {

	private final BiodataRepository biodataRepository;
	private final UserRepository userRepository;
	private final BiodataMapper mapper;

	@Transactional(readOnly = true)
	public BiodataResponse getMine(UUID userId) {
		return mapper.toResponse(getByUserId(userId));
	}

	@Transactional
	public BiodataResponse updateMine(UUID userId, UpdateBiodataRequest request) {
		Biodata biodata = getByUserId(userId);
		applyPatch(biodata, request);
		return mapper.toResponse(biodataRepository.save(biodata));
	}

	@Transactional
	public ResponseEntity<?> complete(UUID userId) {
		Biodata biodata = getByUserId(userId);
		List<String> missing = missingRequiredFields(biodata);
		if (!missing.isEmpty()) {
			return ResponseEntity.badRequest().body(new MissingFieldsResponse("error", missing));
		}
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		user.setRegistrationStep(RegistrationStep.COMPLETED);
		userRepository.save(user);
		return ResponseEntity.ok(new CompleteRegistrationResponse("success", "Registration completed", "completed"));
	}

	private Biodata getByUserId(UUID userId) {
		return biodataRepository.findByUserId(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Biodata not found"));
	}

	private void applyPatch(Biodata biodata, UpdateBiodataRequest request) {
		if (request.fullName() != null) biodata.setFullName(request.fullName());
		if (request.gender() != null) biodata.setGender(parseGender(request.gender()));
		if (request.age() != null) biodata.setAge(request.age());
		if (request.gotra() != null) biodata.setGotra(request.gotra());
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
		if (biodata.getAge() == null) missing.add("age");
		if (!StringUtils.hasText(biodata.getGotra())) missing.add("gotra");
		if (!StringUtils.hasText(biodata.getProfession())) missing.add("profession");
		if (biodata.getAnnualIncome() == null) missing.add("annualIncome");
		if (!StringUtils.hasText(biodata.getLocation())) missing.add("location");
		if (!StringUtils.hasText(biodata.getEducation())) missing.add("education");
		if (!StringUtils.hasText(biodata.getPhotoUrl())) missing.add("photoUrl");
		return missing;
	}
}
