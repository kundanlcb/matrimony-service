package com.cm.matrimony_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application class for the Matrimony Service.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class MatrimonyServiceApplication {

	/**
	 * Main entry point of the application.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(MatrimonyServiceApplication.class, args);
	}

}
