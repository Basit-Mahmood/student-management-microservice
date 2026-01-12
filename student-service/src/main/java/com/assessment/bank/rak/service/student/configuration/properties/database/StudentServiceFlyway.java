package com.assessment.bank.rak.service.student.configuration.properties.database;

public record StudentServiceFlyway(
	
	boolean enabled,
	boolean baselineOnMigrate,
	Integer transactionTimeout
		
) {}
