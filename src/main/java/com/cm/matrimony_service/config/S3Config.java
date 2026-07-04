package com.cm.matrimony_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Configuration class for AWS S3 setup.
 */
@Configuration
public class S3Config {

	/**
	 * Provides an S3Presigner instance for generating presigned URLs.
	 * 
	 * @param appProperties Application properties for S3 configuration
	 * @return Configured S3Presigner
	 */
	@Bean
	public S3Presigner s3Presigner(AppProperties appProperties) {
		return S3Presigner.builder()
				.region(Region.of(appProperties.upload().region()))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
}
