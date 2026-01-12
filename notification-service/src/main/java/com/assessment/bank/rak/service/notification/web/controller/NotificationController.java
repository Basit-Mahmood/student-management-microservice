package com.assessment.bank.rak.service.notification.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.assessment.bank.rak.service.notification.service.EmailService;
import com.assessment.bank.rak.service.notification.web.request.FeesPaymentNotificationRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification API(s)", description = "Endpoints for sending automated notifications (Email/SMS)")
public class NotificationController {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-receipt")
    @ResponseStatus(HttpStatus.ACCEPTED) // 202 Accepted indicates processing started
    @Operation(
        summary = "Send payment receipt email",
        description = "Processes the payment data and sends a formatted HTML receipt via email. " +
                      "Returns 202 Accepted as the email process is offloaded to a background thread."
    )
    @ApiResponse(responseCode = "202", description = "Email notification has been queued for delivery")
    @ApiResponse(responseCode = "400", description = "Invalid request body provided")
    @ApiResponse(responseCode = "500", description = "Internal error while preparing the notification")
    public Mono<Void> sendReceipt(@Valid @RequestBody FeesPaymentNotificationRequest request) {
    	
    		LOGGER.info("Received request to send receipt for student: {}", request.studentName());
    		
    		return emailService.sendSuccessfulFeesPaymentEmail(request)
                    .doOnSuccess(/*unused*/_ -> LOGGER.info("Notification flow completed for student: {}", request.studentName()))
                    .doOnError(err -> LOGGER.error("Notification flow failed for student: {}", request.studentName(), err));
    	
    }
	
}
