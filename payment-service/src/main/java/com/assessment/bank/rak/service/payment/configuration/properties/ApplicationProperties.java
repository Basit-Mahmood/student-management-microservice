package com.assessment.bank.rak.service.payment.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.assessment.bank.rak.service.payment.configuration.properties.api.Api;

@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(Api api) {

}
