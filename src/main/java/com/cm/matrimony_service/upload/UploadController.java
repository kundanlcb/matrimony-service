package com.cm.matrimony_service.upload;

import com.cm.matrimony_service.common.security.AuthenticatedUser;
import com.cm.matrimony_service.upload.UploadDtos.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

	private final UploadService uploadService;

	@GetMapping("/presigned-url")
	PresignedUrlResponse presignedUrl(@AuthenticationPrincipal AuthenticatedUser user,
		@RequestParam String fileName,
		@RequestParam String contentType) {
		return uploadService.createPresignedUrl(user.id(), fileName, contentType);
	}
}
