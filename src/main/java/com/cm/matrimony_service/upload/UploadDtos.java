package com.cm.matrimony_service.upload;

public final class UploadDtos {

	private UploadDtos() {
	}

	public record PresignedUrlResponse(String uploadUrl, String fileUrl) {
	}
}
