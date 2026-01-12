package com.assessment.bank.rak.service.payment.service;

import com.assessment.bank.rak.service.payment.web.request.PaymentRequest;

import reactor.core.publisher.Mono;

public interface PaymentService {

	Mono<String> processPayment(PaymentRequest request);
	
}
