package com.cm.matrimony_service.upload;

/**
 * Data Transfer Objects (DTOs) for the upload feature.
 */
public final class UploadDtos {

	private UploadDtos() {
	}

	/**
	 * Response containing the presigned upload URL and the final public URL of the file.
	 *
	 * @param uploadUrl the URL to which the file should be uploaded
	 * @param fileUrl the public URL where the file will be accessible
	 */
	public record PresignedUrlResponse(String uploadUrl, String fileUrl) {
	}
}
