package com.cm.matrimony_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for customizing Jackson ObjectMapper.
 */
@Configuration
public class JacksonConfig {

	/**
	 * Provides a configured ObjectMapper instance with registered modules.
	 * 
	 * @return Configured ObjectMapper
	 */
	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper().findAndRegisterModules();
	}
}
