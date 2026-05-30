package com.cm.matrimony_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MatrimonyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatrimonyServiceApplication.class, args);
	}

}
