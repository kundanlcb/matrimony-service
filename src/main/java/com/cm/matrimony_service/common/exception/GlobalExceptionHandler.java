package com.cm.matrimony_service.common.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler to map exceptions to standard HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handles custom API exceptions.
	 *
	 * @param ex the custom ApiException
	 * @return a response entity containing the status code and error message
	 */
	@ExceptionHandler(ApiException.class)
	ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(error(ex.getMessage()));
	}

	/**
	 * Handles validation exceptions for method arguments.
	 *
	 * @param ex the validation exception
	 * @return a response entity containing the validation errors
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		List<String> fields = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getField).distinct().toList();
		Map<String, Object> body = error("Validation failed");
		body.put("fields", fields);
		return ResponseEntity.badRequest().body(body);
	}

	/**
	 * Handles bad request exceptions such as constraint violations.
	 *
	 * @param ex the exception causing the bad request
	 * @return a response entity containing the error message
	 */
	@ExceptionHandler({ConstraintViolationException.class, DataIntegrityViolationException.class, IllegalArgumentException.class})
	ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
		return ResponseEntity.badRequest().body(error(ex.getMessage()));
	}

	/**
	 * Handles unexpected server errors.
	 *
	 * @param ex the unexpected exception
	 * @return a response entity indicating an internal server error
	 */
	@ExceptionHandler(Exception.class)
	ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("Unexpected server error"));
	}

	private Map<String, Object> error(String message) {
		Map<String, Object> body = new HashMap<>();
		body.put("status", "error");
		body.put("message", message);
		return body;
	}
}
