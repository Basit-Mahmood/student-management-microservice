package com.assessment.bank.rak.service.student.configuration.database;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.tools.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import com.assessment.bank.rak.service.student.configuration.properties.ApplicationProperties;

@Configuration
@Profile("local") // Only enable this for local development
public class H2ConsoleConfiguration {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private final ApplicationProperties applicationProperties;
	
    private Server webServer;

    // Use a different port than your main application (9001) to avoid conflicts
    private final String h2ConsolePort;
    
    public H2ConsoleConfiguration(ApplicationProperties applicationProperties) {
		
		this.applicationProperties = applicationProperties;
		this.h2ConsolePort = this.applicationProperties.database().h2Console().port();
		
	}

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws SQLException {
        LOGGER.info("Starting H2 console on port {}", h2ConsolePort);
        // "-webAllowOthers" allows connections from outside the container if needed
        // Note: H2 standalone server does not support a custom context path via command line flags.
        // It always runs at the root (/) of the specified port.
        this.webServer = Server.createWebServer(
                "-webPort", h2ConsolePort, 
                "-tcpAllowOthers",
                "-webAllowOthers"
        ).start();

        // We can display the intended URL in logs for documentation consistency
        String manualUrl = String.format("http://localhost:%s/student-service/h2-console", h2ConsolePort);
        LOGGER.info("H2 Console internal server started.");
        LOGGER.info("Access H2 Console at: http://localhost:{}", h2ConsolePort);
        LOGGER.info("Note: Context path mapping is only visual in Netty; H2 remains at root of port {}", h2ConsolePort);
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        if (this.webServer != null) {
            LOGGER.info("Stopping H2 console");
            this.webServer.stop();
        }
    }
	
}
