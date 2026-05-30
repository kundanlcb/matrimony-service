package com.cm.matrimony_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Bean
	public S3Presigner s3Presigner(AppProperties appProperties) {
		return S3Presigner.builder()
				.region(Region.of(appProperties.upload().region()))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
}
