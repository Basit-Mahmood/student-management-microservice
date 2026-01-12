package com.assessment.bank.rak.service.notification.service;

import com.assessment.bank.rak.service.notification.web.request.FeesPaymentNotificationRequest;

import reactor.core.publisher.Mono;

public interface EmailService {
	
	Mono<Void> sendSuccessfulFeesPaymentEmail(FeesPaymentNotificationRequest feesPaymentNotificationRequest);

}
