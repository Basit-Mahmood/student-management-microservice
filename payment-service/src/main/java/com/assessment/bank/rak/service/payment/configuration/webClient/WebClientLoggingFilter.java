package com.assessment.bank.rak.service.payment.configuration.webClient;

import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import reactor.core.publisher.Mono;

public class WebClientLoggingFilter {
	
	private static final Logger LOGGER = LogManager.getLogger();

	// Logs request details
    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
        		LOGGER.info("[WebClient Request] {} {}", request.method(), request.url());
            if (LOGGER.isDebugEnabled()) {
            		LOGGER.debug("Headers: {}", request.headers());
            }
            return Mono.just(request);
        });
    }

    // Logs response details including status and duration
    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            Instant startTime = Instant.now();

            return Mono.deferContextual(contextView -> {
                Duration duration = Duration.between(
                        contextView.getOrDefault("startTime", startTime), 
                        Instant.now()
                );
                LOGGER.info("[WebClient Response] Status: {} (in {} ms)", 
                        response.statusCode(), duration.toMillis());

                if (LOGGER.isDebugEnabled()) {
                    response.headers().asHttpHeaders()
                            .forEach((k, v) -> LOGGER.debug("{}: {}", k, v));
                }

                return Mono.just(response);
            });
        });
    }

    //  Adds timing context for request duration measurement
    public static ExchangeFilterFunction timingFilter() {
        return (request, next) ->
                Mono.deferContextual(_ -> next.exchange(request))
                        .contextWrite(ctx -> ctx.put("startTime", Instant.now()));
    }
	
}
