package com.assessment.bank.rak.service.notification.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.assessment.bank.rak.service.notification.service.EmailService;
import com.assessment.bank.rak.service.notification.web.request.FeesPaymentNotificationRequest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class EmailServiceImpl implements EmailService {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
	public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
	}

	@Override
	public Mono<Void> sendSuccessfulFeesPaymentEmail(FeesPaymentNotificationRequest feesPaymentNotificationRequest) {
		
		return Mono.fromRunnable(() -> {
            
			String studentName = feesPaymentNotificationRequest.studentName();
			String studentEmail = feesPaymentNotificationRequest.studentEmail();
			
			LOGGER.info("Processing email template for student: {}", studentName);
			
			try {
          
                Context context = new Context();
                context.setVariable("studentName", studentName);
                context.setVariable("dateTime", feesPaymentNotificationRequest.dateTime());
                context.setVariable("refNo", feesPaymentNotificationRequest.referenceNo());
                context.setVariable("cardNo", feesPaymentNotificationRequest.cardNo());
                context.setVariable("cardType", feesPaymentNotificationRequest.cardType());
                context.setVariable("amount", feesPaymentNotificationRequest.tuitionFees());
                context.setVariable("grade", feesPaymentNotificationRequest.grade());

                String htmlContent = templateEngine.process("fees-payment-email-template", context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(studentEmail); // Assuming 'email' field exists in request
                helper.setSubject("Fees Payment Successful - " + studentName);
                helper.setText(htmlContent, true);

                LOGGER.debug("Attempting to send email to {}", studentEmail);
                mailSender.send(message);
                LOGGER.info("Email sent successfully to {}", studentEmail);

            } catch (MessagingException e) {
                LOGGER.error("Failed to construct or send email for student: {}", studentName, e);
                throw new RuntimeException("Email sending failed", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic()) // Offload blocking I/O
        .then();
		

	}

}
