package com.assessment.bank.rak.service.student.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Request object for creating a new student")
public record StudentRequest(
	
	@NotBlank(message = "Student name is required")
    @Schema(description = "Name of Student", example = "Ali Haider")
    String studentName,
    
    @NotBlank(message = "Student email is required")
    @Schema(description = "Email of Student", example = "ali_heider@example.com")
    String studentEmail,
    
    @NotBlank(message = "Grade is required")
    @Schema(description = "Grade of Student", type = "string", defaultValue = "1", allowableValues = {"1", "2", "3"})
    String grade,
    
    @NotEmpty(message = "Mobile number cannot be empty")
    @Schema(description = "Mobile Number", example = "+971234567")
    String mobileNumber,
    
    @Schema(description = "School name. If null, internal logic may apply defaults.", type = "string", defaultValue = "ABC School")
    String schoolName
		
) {}
