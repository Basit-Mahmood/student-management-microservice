package com.assessment.bank.rak.service.notification.web.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request object for sending a successful fees payment email notification")
public record FeesPaymentNotificationRequest(
	
	@NotBlank(message = "Student name is required")
    @Schema(description = "Full name of the student", example = "Ali Haider")
	String studentName,
	
	@Email(message = "Invalid email format")
    @NotBlank(message = "Student email is required")
    @Schema(description = "Email address where the receipt will be sent", example = "ali_haider@example.com")
	String studentEmail,
	
	@NotBlank(message = "Student ID is required")
    @Schema(description = "Unique identifier of the student", example = "STU-1001")
    String studentId,
    
    @NotBlank(message = "Reference number is required")
    @Schema(description = "Bank or System transaction reference number", example = "REF-998877")
    String referenceNo,
    
    @NotBlank(message = "Card number is required")
    @Schema(description = "Masked card number used for payment", example = "4111 XXXX XXXX 1234")
    String cardNo,
    
    @Schema(description = "Type of card used", example = "VISA", allowableValues = {"VISA", "MASTERCARD", "AMEX"})
    String cardType,
    
    @Positive(message = "Tuition fees must be greater than zero")
    @Schema(description = "Amount paid in AED", example = "5500.00")
    Double tuitionFees,
    
    @Schema(description = "Current grade of the student", example = "Grade 5")
    String grade,
    
    @NotNull(message = "Date and time is required")
    @Schema(description = "Timestamp of the transaction", example = "2026-01-12T10:30:00")
    LocalDateTime dateTime
		
) {}
