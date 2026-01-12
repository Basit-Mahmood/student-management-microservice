package com.assessment.bank.rak.service.student.configuration.database;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FlywayAutoRunner {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Autowired
    private Map<String, Flyway> flywayBeans;

    @PostConstruct
    public void runAllFlywayMigrations() {
    		
    		LOGGER.info(">>> Starting centralized FlywayAutoRunner");

        flywayBeans.forEach((name, flyway) -> {
            try {
            		LOGGER.info("Running Flyway migration for bean: {}", name);
            		var result = flyway.migrate();

                LOGGER.info("{} -> Applied migrations: {}", name, result.migrationsExecuted);
                LOGGER.info("{} -> Target version: {}", name, result.targetSchemaVersion);
            }
            catch (Exception ex) {
            		LOGGER.error("Flyway migration FAILED for bean: {}", name, ex);
                throw ex;
            }
        });

        LOGGER.info("<<< Completed centralized FlywayAutoRunner");
    }
	
}
