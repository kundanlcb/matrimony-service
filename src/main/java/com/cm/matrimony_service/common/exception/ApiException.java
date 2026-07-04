package com.cm.matrimony_service.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception class for representing API-level errors.
 * Encapsulates an HTTP status code and an error message.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
