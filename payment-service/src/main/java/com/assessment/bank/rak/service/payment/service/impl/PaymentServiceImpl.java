package com.assessment.bank.rak.service.payment.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.assessment.bank.rak.service.payment.configuration.properties.api.NotificationService;
import com.assessment.bank.rak.service.payment.configuration.properties.api.StudentService;
import com.assessment.bank.rak.service.payment.service.PaymentService;
import com.assessment.bank.rak.service.payment.web.request.FeesPaymentNotificationRequest;
import com.assessment.bank.rak.service.payment.web.request.PaymentRequest;
import com.assessment.bank.rak.service.payment.web.request.StudentDTO;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import reactor.core.publisher.Mono;

@Service
public class PaymentServiceImpl implements PaymentService {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private final StudentService studentService;
	private final WebClient studentServiceWebClient;
	private final NotificationService notificationService;
    private final WebClient notificationServiceWebClient;
    private final ReactiveCircuitBreaker notificationServiceCB;
    private final RetryRegistry retryRegistry; // Inject the registry
    
	public PaymentServiceImpl(StudentService studentService, WebClient studentServiceWebClient,
			NotificationService notificationService, WebClient notificationServiceWebClient,
			ReactiveCircuitBreakerFactory<?, ?> cbFactory, RetryRegistry retryRegistry) {
		this.studentService = studentService;
		this.studentServiceWebClient = studentServiceWebClient;
		this.notificationService = notificationService;
		this.notificationServiceWebClient = notificationServiceWebClient;
		
		// Create the Circuit Breaker instance
        this.notificationServiceCB = cbFactory.create("notificationService");
        this.retryRegistry = retryRegistry;
	}

	@Override
	public Mono<String> processPayment(PaymentRequest request) {
		
		String uriTemplate = studentService.endpoints().get("get-student-by-id");
        LOGGER.info("Calling student-service API (get-student-by-id) for id={} -> {}", request.studentId(), uriTemplate);
        
        // Fetch the retry configuration from YAML by name
        var studentServiceRetry = retryRegistry.retry("studentService");
        
        return studentServiceWebClient.get()
                .uri(builder -> {
                    builder.path(uriTemplate.replace("{studentId}", request.studentId()));
                    return builder.build();
                })
                .retrieve()
                // Handle 404 from student-service
                .onStatus(HttpStatusCode::is4xxClientError, /*response*/_ -> 
                    Mono.error(new RuntimeException("Student not found with ID: " + request.studentId())))
                .bodyToMono(StudentDTO.class)
                // LAYER 1: Stream-level timeout (must be slightly higher than Netty timeout)
                .timeout(Duration.ofSeconds(12))
                // APPLY RETRY HERE
                .transformDeferred(RetryOperator.of(studentServiceRetry))
                // LAYER 2: Handle Timeout specifically
                .onErrorMap(java.util.concurrent.TimeoutException.class, /*e*/_ -> 
                    new RuntimeException("Student Service timed out after retries"))
                .flatMap(student -> {
                    LOGGER.info("Student verified: {}. Processing payment...", student.name());
                    
                    // Logic for actual payment processing would go here
                    String referenceNo = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                    FeesPaymentNotificationRequest notifyRequest = createNotifyRequest(student, request, referenceNo);

                    return sendNotification(notifyRequest)
                            .thenReturn("Payment successful. Reference: " + referenceNo);
                });
		
	}
	
	private Mono<Void> sendNotification(FeesPaymentNotificationRequest request) {
		
		String uri = notificationService.endpoints().get("email-fees-payment-receipt");
		
        return notificationServiceWebClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                // Add stream timeout here as well
                .timeout(Duration.ofSeconds(12))
                .transform(it -> notificationServiceCB.run(it, throwable -> {
                	
                		// FALLBACK LOGIC: If notification fails, we don't fail the payment
                		if (throwable instanceof java.util.concurrent.TimeoutException) {
                        LOGGER.error("Notification Service timed out!");
                    } else {
                        LOGGER.error("Notification Service unavailable! Payment was successful but email not sent.", throwable);
                    }
                    return Mono.empty();
                	
                }))
                .doOnSuccess(_ -> LOGGER.info("Notification sent for student: {}", request.studentName()))
                .doOnError(e -> LOGGER.error("Failed to trigger notification", e));
    }
	
	private FeesPaymentNotificationRequest createNotifyRequest(StudentDTO student, PaymentRequest request , String referenceNo) {
		
		return new FeesPaymentNotificationRequest(
                student.name(),
                student.email(),
                student.studentId(),
                referenceNo,
                request.cardNo(),
                request.cardType(),
                request.amount(),
                student.grade(),
                LocalDateTime.now()
            );
		
	}

}
