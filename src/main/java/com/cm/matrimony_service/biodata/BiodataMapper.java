package com.cm.matrimony_service.biodata;

import com.cm.matrimony_service.biodata.BiodataDtos.BiodataResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BiodataMapper {

	public BiodataResponse toResponse(Biodata biodata) {
		return new BiodataResponse(
			biodata.getId(),
			biodata.getFullName(),
			toDisplayName(biodata.getGender()),
			biodata.getAge(),
			biodata.getGotra(),
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
			biodata.getInterests() == null ? List.of() : List.copyOf(biodata.getInterests()));
	}

	private String toDisplayName(Gender gender) {
		if (gender == null) {
			return null;
		}
		return gender.name().charAt(0) + gender.name().substring(1).toLowerCase();
	}
}
