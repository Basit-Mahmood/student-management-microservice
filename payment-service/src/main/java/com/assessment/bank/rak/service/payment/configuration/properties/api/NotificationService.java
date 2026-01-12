package com.assessment.bank.rak.service.payment.configuration.properties.api;

import java.net.URI;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "app.api.notification-service")
public record NotificationService(@NotNull URI baseUri, Map<String, String> endpoints) {

}
