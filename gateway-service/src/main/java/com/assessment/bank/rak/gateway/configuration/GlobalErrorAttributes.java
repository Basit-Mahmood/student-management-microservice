package com.assessment.bank.rak.gateway.configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

	@Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        
		Map<String, Object> map = super.getErrorAttributes(request, options);
        
        // Extract the original status and error
        int status = (int) map.getOrDefault("status", 500);
        
        // Clean up the response - remove default fields we don't want to expose
        map.clear();
        
        map.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        map.put("status", status);
        map.put("path", request.path());
        
        // Mask specific errors with user-friendly messages
        if (status == 500) {
            map.put("message", "We are experiencing technical difficulties. Please try again later.");
            map.put("errorCode", "INTERNAL_SERVER_ERROR");
        } else if (status == 503) {
            map.put("message", "The service is temporarily unavailable. Our team is working on it.");
            map.put("errorCode", "SERVICE_UNAVAILABLE");
        } else {
            map.put("message", "An unexpected error occurred.");
            map.put("errorCode", "GATEWAY_ERROR");
        }

        return map;
    }
	
}
