package com.assessment.bank.rak.service.notification.configuration;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this is the first filter to run
public class CorrelationIdFilter implements WebFilter {

	private static final Logger LOGGER = LogManager.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String USERNAME_HEADER = "X-User-Name"; // Optional header
    private static final String MDC_ID_KEY = "id";
    private static final String MDC_USER_KEY = "username";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 1. Get or Generate ID
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        
     // 2. Handle Username (with default)
        String username = exchange.getRequest().getHeaders().getFirst(USERNAME_HEADER);
        if (!StringUtils.hasText(username)) {
            username = "anonymous"; // Your default value
        }

        // 2. Add to response header so the client can track it too
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        final String finalId = correlationId;
        final String finalUser = username;

        // 3. Chain the request and write to Reactor Context
        return chain.filter(exchange)
                .contextWrite(Context.of(MDC_ID_KEY, finalId, MDC_USER_KEY, finalUser))
                .doOnSubscribe(/*sig*/_ -> LOGGER.debug("Started request with Correlation ID: {}", finalId));
    }
	
}
