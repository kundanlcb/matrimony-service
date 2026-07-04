package com.cm.matrimony_service.common.web;

/**
 * A standard API response wrapper for status messages.
 */
public record ApiStatusResponse(String status, String message) {
	public static ApiStatusResponse success(String message) {
		return new ApiStatusResponse("success", message);
	}
}
