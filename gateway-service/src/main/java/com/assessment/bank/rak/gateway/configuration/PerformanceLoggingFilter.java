package com.assessment.bank.rak.gateway.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class PerformanceLoggingFilter implements GlobalFilter, Ordered {

	private static final Logger LOGGER = LogManager.getLogger();
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Pre-filter logic: Record the start time
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());
        
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 2. Post-filter logic: Executed after the downstream response is received
            Long startTime = exchange.getAttribute(START_TIME_ATTR);
            if (startTime != null) {
                long executeTime = System.currentTimeMillis() - startTime;
                LOGGER.info("Gateway Filter: Method: {}, Path: {}, Execution Time: {}ms, Status: {}", 
                    method, 
                    path, 
                    executeTime, 
                    exchange.getResponse().getStatusCode());
            }
        }));
    }

    @Override
    public int getOrder() {
        // High precedence to ensure we capture the full lifecycle
        return Ordered.LOWEST_PRECEDENCE;
    }
	
}
