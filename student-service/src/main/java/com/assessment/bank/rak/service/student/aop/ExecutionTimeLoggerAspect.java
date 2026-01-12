package com.assessment.bank.rak.service.student.aop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class ExecutionTimeLoggerAspect {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Around("@annotation(ExecutionTimeLogger)")
	public Object executionTimeLogger(ProceedingJoinPoint joinPoint) throws Throwable {
	    long startTime = System.currentTimeMillis();
	    Object result = joinPoint.proceed();

	    if (result instanceof Mono<?> mono) {
	        return mono.doOnSubscribe(/*s*/_ -> LOGGER.debug("Started: {}", joinPoint.getSignature()))
	                   .doFinally(/*signalType*/_ -> logTime(joinPoint, startTime));
	    } else if (result instanceof Flux<?> flux) {
	        return flux.doOnSubscribe(/*s*/_ -> LOGGER.debug("Started: {}", joinPoint.getSignature()))
	                   .doFinally(/*signalType*/_ -> logTime(joinPoint, startTime));
	    }

	    // Fallback for non-reactive methods
	    logTime(joinPoint, startTime);
	    return result;
	}

	private void logTime(ProceedingJoinPoint joinPoint, long startTime) {
	    long executionTime = System.currentTimeMillis() - startTime;
	    LOGGER.info("{} executed in {} ms", joinPoint.getSignature().toShortString(), executionTime);
	}
	
}
