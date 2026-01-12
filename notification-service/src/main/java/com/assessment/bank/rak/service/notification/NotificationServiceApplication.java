package com.assessment.bank.rak.service.notification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

import reactor.core.publisher.Hooks;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NotificationServiceApplication {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String APPLICATION_PID_FILE = "./pid/notification-service.pid";

	public static void main(String[] args) {
		
		LOGGER.info("Starting NotificationServiceApplication with {} arguments.", args != null ? args.length : 0);

	    // This makes %X{id} work in reactive threads!
	    Hooks.enableAutomaticContextPropagation();

	    ConfigurableApplicationContext context = new SpringApplicationBuilder(NotificationServiceApplication.class)
	            // Fix: Use .web() instead of .setWebApplicationType()
	            .web(WebApplicationType.REACTIVE) 
	            .listeners(new ApplicationPidFileWriter(APPLICATION_PID_FILE))
	            .run(args);

	    LOGGER.info("NotificationServiceApplication started successfully. Application context ID: {}", context.getId());
		
	}

}
