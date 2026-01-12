package com.assessment.bank.rak.service.student.configuration.database;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class FlywayConfiguration {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String STUDENT_SERVICE_FLYWAY_PROP_PREFIX = "app.database.student-service-flyway";
	
	/**
	 * 	spring:
  		  flyway:
    		    enabled: true
    			table: flyway_schema_history
    			baseline-on-migrate: true
    			clean-disabled: true 
    			locations:
      		  - classpath:database/flyway/common
      		  - classpath:database/flyway/${spring.profiles.active}
	 * 
	 */
	@Bean(name = "flywayStudentService")
    @ConditionalOnProperty(name = STUDENT_SERVICE_FLYWAY_PROP_PREFIX + ".enabled", havingValue = "true", matchIfMissing = true)
    public Flyway flywayStudentService(@Qualifier("studentServiceHikariDataSource") DataSource dataSource, Environment env) {

		String activeProfile = env.getActiveProfiles().length > 0
                ? env.getActiveProfiles()[0]
                : "default";

		LOGGER.info("Initializing Flyway for Student Service DB");
		LOGGER.info("Active profile: {}", activeProfile);
		
		// Map Spring profile → Flyway directory (02_, 03_, 04_, 05_)
        String resolvedProfileFolder = switch (activeProfile) {
            case "local" -> "02_local";
            case "dev"   -> "03_dev";
            case "uat"   -> "04_uat";
            case "prod"  -> "05_prod";
            default      -> "01_common"; // fallback for unknown profile
        };
       
        LOGGER.info("Flyway profile folder resolved to: {}", activeProfile);
		
        Location commonScriptsLocation = Location.fromPath("classpath:", "database/flyway/student-service/01_common");
        Location seedScriptsLocation = Location.fromPath("classpath:", "database/flyway/student-service/" + resolvedProfileFolder );
        
        Location[] locations = new Location[]{commonScriptsLocation, seedScriptsLocation};

	     // STEP 1 — Build a *temporary* configuration for CustomScanner
	     ClassicConfiguration tmpConfig = new ClassicConfiguration();
	     tmpConfig.setDataSource(dataSource);
	     tmpConfig.setLocations(locations);
	     tmpConfig.setBaselineOnMigrate(env.getProperty(STUDENT_SERVICE_FLYWAY_PROP_PREFIX + ".baseline-on-migrate", Boolean.class, true));
	     tmpConfig.setTable("flyway_student_service_history");

	     // STEP 2 — Create your CustomScanner
	     CustomScanner<JavaMigration> scanner = new CustomScanner<>(JavaMigration.class, tmpConfig, locations);
        
		Flyway flyway = Flyway.configure()
			.configuration(tmpConfig)
	        //.dataSource(dataSource)
	        .resourceProvider(scanner)
            //.locations(commonScriptsLocation, seedScriptsLocation)
           // .baselineOnMigrate(
                   // env.getProperty(FLYWAY_AUTHORIZATION_OAUTH2_PROP_PREFIX + ".baseline-on-migrate", Boolean.class, true)
            //)
            //.table("flyway_authorization_oauth2_history")
            .load();

        return flyway;
		
    }
	
}
