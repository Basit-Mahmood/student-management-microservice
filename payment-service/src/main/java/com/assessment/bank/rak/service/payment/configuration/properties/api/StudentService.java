package com.assessment.bank.rak.service.payment.configuration.properties.api;

import java.net.URI;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "app.api.student-service")
public record StudentService(@NotNull URI baseUri, Map<String, String> endpoints) {

}
