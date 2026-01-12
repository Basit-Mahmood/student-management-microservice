package com.assessment.bank.rak.gateway.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayConfiguration {

	@Value("${SERVICES_STUDENT:http://localhost:9001}")
	private String studentServiceUri;
	
	@Value("${SERVICES_PAYMENT:http://localhost:9002}")
	private String paymentServiceUri;
	
	@Value("${SERVICES_NOTIFICATION:http://localhost:9003}")
	private String notificatonServiceUri;
	
	@Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // --- Student Service ---
            .route("student-service", r -> r
                .path("/student-service/api/students/**", "/student-service/v3/api-docs")
                .filters(f -> applyCommonFilters(f)
                    .circuitBreaker(config -> config
                        .setName("studentServiceCircuitBreaker")
                        .setFallbackUri("forward:/student-service-fallback"))
                )
                .uri(studentServiceUri)) // Use @Value or GatewaySettings here

            // --- Payment Service ---
            .route("payment-service", r -> r
                .path("/payment-service/api/payments/**", "/payment-service/v3/api-docs")
                .filters(f -> applyCommonFilters(f)
                    .circuitBreaker(config -> config
                        .setName("paymentServiceCircuitBreaker")
                        .setFallbackUri("forward:/payment-service-fallback")
                    ) 
                )
                .uri(paymentServiceUri))
            
            // --- Notification Service ---
            .route("notification-service", r -> r
                .path("/notification-service/api/notifications/**", "/notification-service/v3/api-docs")
                .filters(f -> applyCommonFilters(f)
                    .circuitBreaker(config -> config
                        .setName("notificationServiceCircuitBreaker")
                        .setFallbackUri("forward:/notification-service-fallback")
                    )                  
                )
                .uri(notificatonServiceUri))

            .build();
    }
	
	@Bean
	public CorsWebFilter corsWebFilter() {
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true);
	    config.addAllowedOrigin("http://localhost:3000"); // Your Frontend
	    config.addAllowedOrigin("http://localhost:9000"); // Swagger UI itself
	    config.addAllowedHeader("*");
	    config.addAllowedMethod("*");

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);
	    return new CorsWebFilter(source);
	}
	
	// This mimics your "default-filters" logic
	@SuppressWarnings("unchecked") // Resolves the Varargs type safety warning
    private GatewayFilterSpec applyCommonFilters(GatewayFilterSpec f) {
        return f.retry(retryConfig -> retryConfig
            .setRetries(3)
            .setMethods(HttpMethod.GET)
            .setSeries(HttpStatus.Series.SERVER_ERROR)
            .setExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
            .setBackoff(
                Duration.ofMillis(50),  // firstBackoff
                Duration.ofMillis(500), // maxBackoff
                2,                      // factor
                false                   // basedOnPreviousValue
            )
        ).fallbackHeaders(h -> {
            h.setExecutionExceptionTypeHeaderName("X-Exception-Type");
            h.setExecutionExceptionMessageHeaderName("X-Exception-Message");
        });
    }
	
}
