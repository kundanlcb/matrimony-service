package com.cm.matrimony_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Otp otp, Upload upload, Admin admin) {

	public record Jwt(String secret, long expirationSeconds) {
	}

	public record Otp(int expiresInSeconds, String fixedCode) {
	}

	public record Upload(String publicBaseUrl, String bucketName, String region) {
	}

	public record Admin(String username, String password) {
	}
}
