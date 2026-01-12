package com.assessment.bank.rak.service.student.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.assessment.bank.rak.service.student.configuration.properties.database.Database;

@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
	
	Database database
		
) {}
