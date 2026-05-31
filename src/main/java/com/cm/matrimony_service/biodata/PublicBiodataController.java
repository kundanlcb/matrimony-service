package com.cm.matrimony_service.biodata;

import com.cm.matrimony_service.biodata.BiodataDtos.PublicBiodataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/biodata/public")
@RequiredArgsConstructor
public class PublicBiodataController {

	private final BiodataService biodataService;

	@GetMapping("/{userId}")
	public PublicBiodataResponse getPublicProfile(@PathVariable UUID userId) {
		return biodataService.getPublicBiodata(userId);
	}
}
