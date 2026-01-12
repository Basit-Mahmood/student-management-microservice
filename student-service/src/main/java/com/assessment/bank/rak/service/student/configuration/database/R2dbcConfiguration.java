package com.assessment.bank.rak.service.student.configuration.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.boot.r2dbc.autoconfigure.R2dbcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;

import com.assessment.bank.rak.service.student.database.r2dbc.repository.R2dbcRepositoryMarker;

import io.r2dbc.spi.ConnectionFactory;

@Configuration
@EnableR2dbcRepositories(basePackageClasses = { R2dbcRepositoryMarker.class })
public class R2dbcConfiguration {

	private static final Logger LOGGER = LogManager.getLogger();
	
	// Inject the auto-configured properties from your YAML
    private final R2dbcProperties r2dbcProperties;

    public R2dbcConfiguration(R2dbcProperties r2dbcProperties) {
        this.r2dbcProperties = r2dbcProperties;
    }
	
	@Bean
	@DependsOn("flywayStudentService") // Ensures Flyway runs before R2DBC starts
    public ConnectionFactory connectionFactory() {
        
		LOGGER.info("Initializing R2DBC ConnectionFactory using URL: {}", r2dbcProperties.getUrl());

		// This helper method automatically handles pooling and driver discovery
	    // based on the URL provided in your YAML
	    return ConnectionFactoryBuilder.withUrl(r2dbcProperties.getUrl())
	            .username(r2dbcProperties.getUsername())
	            .password(r2dbcProperties.getPassword())
	            .build();
		
    }

    @Bean
    public R2dbcTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
	
}
