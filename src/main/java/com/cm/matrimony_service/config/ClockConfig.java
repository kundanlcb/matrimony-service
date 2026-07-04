package com.cm.matrimony_service.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Clock bean.
 */
@Configuration
public class ClockConfig {

	/**
	 * Provides a UTC Clock instance.
	 * 
	 * @return UTC Clock
	 */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
