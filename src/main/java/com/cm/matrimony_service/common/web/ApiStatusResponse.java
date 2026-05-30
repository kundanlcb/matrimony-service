package com.cm.matrimony_service.common.web;

public record ApiStatusResponse(String status, String message) {
	public static ApiStatusResponse success(String message) {
		return new ApiStatusResponse("success", message);
	}
}
