package com.assessment.bank.rak.service.payment.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request object for processing a student fee payment")
public record PaymentRequest(
	
	@NotBlank(message = "Student ID is required")
    @Schema(description = "Unique identifier of the student", example = "1")
	String studentId,
	
	@Positive(message = "Payment amount must be greater than zero")
    @Schema(description = "Tuition fees amount to be paid in AED", example = "1500.50")
    Double amount,
    
    @NotBlank(message = "Card number is required")
    @Schema(description = "Credit/Debit card number used for the transaction", example = "4111222233334444")
    String cardNo,
    
    @NotBlank(message = "Card type is required")
    @Schema(description = "The brand/type of the card", example = "VISA", allowableValues = {"VISA", "MASTERCARD", "AMEX"}) 
    String cardType
		
) {}
