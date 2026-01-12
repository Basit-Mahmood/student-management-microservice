package com.assessment.bank.rak.gateway.configuration;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Configuration
public class FallbackConfiguration {

	@Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route()
                // --- Student Service Fallbacks ---
                .path("/student-service-fallback", builder -> builder
                        .GET("", request -> handleFallback(request, "Student lookup is temporarily unavailable."))
                        .POST("", request -> handleFallback(request, "Unable to register student at this time."))
                )
                
                // --- Payment Service Fallbacks ---
                .path("/payment-service-fallback", builder -> builder
                        .POST("", request -> handleFallback(request, "Payment processing is currently down. Your card has not been charged."))
                )
                
                // --- Notification Service Fallbacks ---
                .path("/notification-service-fallback", builder -> builder
                        .POST("", request -> handleFallback(request, "Payment was successful, but the receipt email could not be sent immediately."))
                )
                .build();
    }
    
    /**
     * Common handler that extracts headers injected by the FallbackHeaders filter
     */
    private Mono<ServerResponse> handleFallback(ServerRequest request, String defaultUserMessage) {
        // These header names must match your app-local-gateway.yml configuration
        String exceptionMessage = request.headers().firstHeader("X-Exception-Message");
        String exceptionType = request.headers().firstHeader("X-Exception-Type");

        String refinedMessage = defaultUserMessage;
        
        // Logical branching to provide more specific messages based on the actual error
        if (exceptionMessage != null && exceptionMessage.contains("Timeout")) {
            refinedMessage = "The service took too long to respond. Please try again later.";
        } else if (exceptionType != null && (exceptionType.contains("ConnectException") || exceptionType.contains("ServiceUnavailable"))) {
            refinedMessage = "The service is currently unreachable. Our engineers are working on it.";
        }

        return createFallbackResponse(refinedMessage, exceptionMessage);
    }

    /**
     * Helper method to maintain a consistent error structure.
     */
    private Mono<ServerResponse> createFallbackResponse(String userFriendlyMessage, String exceptionMessage) {
        // Note: Added the missing 'return' keyword here
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Mono.just(Map.of(
                        "message", userFriendlyMessage,
                        "status", 503,
                        "errorCode", "SERVICE_UNAVAILABLE_GATEWAY",
                        "debug_info", exceptionMessage != null ? exceptionMessage : "No extra info"
                )), Map.class);
    }
	
}
