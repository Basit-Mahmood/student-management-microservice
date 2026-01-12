package com.assessment.bank.rak.service.student.configuration.database;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfiguration {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource.student-service")
	public DataSourceProperties studentServiceDataSourceProperties() {
		LOGGER.info("Loading DataSourceProperties for 'student-service'...");
	    return new DataSourceProperties();
	}
	
	@Bean(name = "studentServiceHikariDataSource")
	@Primary
	@ConfigurationProperties("spring.datasource.student-service.hikari")
    public DataSource studentServiceHikariDataSource() {
		
		LOGGER.info("Initializing HikariCP DataSource for 'student-service'...");

        try {
            DataSourceProperties properties = studentServiceDataSourceProperties();

            LOGGER.debug("Student Service DB URL: {}", properties.getUrl());
            LOGGER.debug("Student Service DB Username: {}", properties.getUsername());

            HikariDataSource dataSource = properties
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();

            LOGGER.info("Successfully initialized HikariCP DataSource: {}", dataSource.getPoolName());

            return dataSource;
        }
        catch (Exception ex) {
            LOGGER.error("Failed to initialize student-service DataSource", ex);
            throw ex;
        }
    }
	
}
