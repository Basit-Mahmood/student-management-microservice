package com.assessment.bank.rak.service.payment.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assessment.bank.rak.service.payment.service.PaymentService;
import com.assessment.bank.rak.service.payment.web.request.PaymentRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment API(s)", description = "Endpoints for processing student tuition fees and managing transactions")
public class PaymentController {

	private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(
        summary = "Process student fees", 
        description = "Verifies student existence via Student Service, processes the payment, and triggers a notification."
    )
    @ApiResponse(responseCode = "200", description = "Payment processed successfully and receipt queued")
    @ApiResponse(responseCode = "400", description = "Invalid payment details or student not found")
    @ApiResponse(responseCode = "500", description = "Internal server error during payment orchestration")
    public Mono<ResponseEntity<String>> pay(@Valid @RequestBody PaymentRequest request) {
        return paymentService.processPayment(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof java.util.concurrent.TimeoutException || e.getMessage().contains("timed out")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("Downstream service timeout"));
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()));
                });
    }
	
}
