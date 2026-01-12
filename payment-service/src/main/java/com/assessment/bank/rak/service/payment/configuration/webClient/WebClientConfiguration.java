package com.assessment.bank.rak.service.payment.configuration.webClient;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.assessment.bank.rak.service.payment.configuration.properties.api.NotificationService;
import com.assessment.bank.rak.service.payment.configuration.properties.api.StudentService;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfiguration {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Bean
	public WebClient.Builder webClientBuilder() {
		LOGGER.info("Creating base WebClient.Builder bean");
		
		// 1. Create a Netty HttpClient with timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // Connection Timeout (5s)
                .responseTimeout(Duration.ofSeconds(10))           // Response Timeout (10s)
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(10))); // Read Timeout (10s)
		
        // 2. Return the builder using the custom connector
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
	}
	
	@Bean
	public WebClient studentServiceWebClient(WebClient.Builder builder, StudentService studentService) {
		
		LOGGER.info("Configuring WebClient for student-service...");
        LOGGER.debug("student-service Base URL: {}", studentService.baseUri());

        WebClient.Builder webClientBuilder = builder
                .baseUrl(studentService.baseUri().toString())
                .filter(WebClientLoggingFilter.timingFilter())
                .filter(WebClientLoggingFilter.logRequest())
                .filter(WebClientLoggingFilter.logResponse());

        LOGGER.debug("student-service WebClient logging filters registered");

        WebClient client = webClientBuilder.build();
        LOGGER.info("student-service WebClient created successfully");

        return client;
		
	}
	
	@Bean
	public WebClient notificationServiceWebClient(WebClient.Builder builder, NotificationService notificationService) {
		
		LOGGER.info("Configuring WebClient for notification-service...");
        LOGGER.debug("notification-service Base URL: {}", notificationService.baseUri());

        WebClient.Builder webClientBuilder = builder
                .baseUrl(notificationService.baseUri().toString())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> {
                            LOGGER.debug("Setting notification-service WebClient maxInMemorySize = 16MB");
                            cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                        }).build())
                .filter(WebClientLoggingFilter.timingFilter())
                .filter(WebClientLoggingFilter.logRequest())
                .filter(WebClientLoggingFilter.logResponse());

        LOGGER.debug("notification-service WebClient logging filters registered");

        WebClient client = webClientBuilder.build();
        LOGGER.info("notification-service WebClient created successfully");

        return client;
		
	}

}
