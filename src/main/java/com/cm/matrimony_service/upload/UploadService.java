package com.cm.matrimony_service.upload;

import com.cm.matrimony_service.common.exception.ApiException;
import com.cm.matrimony_service.config.AppProperties;
import com.cm.matrimony_service.upload.UploadDtos.PresignedUrlResponse;
import java.time.Clock;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class UploadService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

	private final AppProperties properties;
	private final Clock clock;
	private final S3Presigner s3Presigner;

	public PresignedUrlResponse createPresignedUrl(UUID userId, String fileName, String contentType) {
		if (!StringUtils.hasText(fileName)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "fileName is required");
		}
		if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "contentType must be image/jpeg, image/png, or image/webp");
		}
		String safeFileName = sanitize(fileName);
		String key = "users/%s/%s".formatted(userId, safeFileName);
		String fileUrl = "%s/%s".formatted(trimSlash(properties.upload().publicBaseUrl()), key);
		
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(properties.upload().bucketName())
				.key(key)
				.contentType(contentType)
				.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(5))
				.putObjectRequest(putObjectRequest)
				.build();

		String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

		return new PresignedUrlResponse(uploadUrl, fileUrl);
	}

	private String sanitize(String fileName) {
		String cleaned = fileName.replace("\\", "/");
		cleaned = cleaned.substring(cleaned.lastIndexOf('/') + 1);
		cleaned = cleaned.replaceAll("[^A-Za-z0-9._-]", "_");
		if (!StringUtils.hasText(cleaned)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid fileName");
		}
		return cleaned;
	}



	private String trimSlash(String value) {
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}
}
