package com.assessment.bank.rak.service.student.web.controller.advice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Handles Validation errors (e.g., @Valid failure)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationExceptions(WebExchangeBindException ex) {
        LOGGER.error("Validation failed for request: {}", ex.getReason());
        
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, "Validation Error");
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        body.put("details", errors);
        
        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    /**
     * Handles invalid inputs (e.g., bad JSON, type mismatch)
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleInputException(ServerWebInputException ex) {
    		
    		// If the error is due to a method argument (like Pageable), we can find the specific reason
        String errorMessage = ex.getReason();
        if (ex.getMethodParameter() != null) {
            errorMessage = "Invalid input for parameter: " + ex.getMethodParameter().getParameterName();
        }

        LOGGER.warn("Invalid input received: {}", errorMessage);
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorBody(HttpStatus.BAD_REQUEST, errorMessage)));
    }

    /**
     * Handles NumberFormatException (e.g., bad ID format in path variable)
     */
    @ExceptionHandler(NumberFormatException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNumberFormat(NumberFormatException ex) {
        LOGGER.error("Invalid ID format provided: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorBody(HttpStatus.BAD_REQUEST, "Invalid ID format. ID must be numeric.")));
    }
    
    /**
     * Handles Type Mismatch (e.g., providing a string for a numeric page/size parameter)
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type '%s'", 
            ex.getName(), ex.getRequiredType().getSimpleName());
        
        LOGGER.warn("Type mismatch error: {}", message);
        
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorBody(HttpStatus.BAD_REQUEST, message)));
    }

    /**
     * General catch-all for any other unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneralException(Exception ex) {
        LOGGER.error("An unexpected error occurred in Student Service: ", ex);
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred.")));
    }

    private Map<String, Object> createErrorBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", java.time.Instant.now().toString()); // Use ISO String
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
	
}
