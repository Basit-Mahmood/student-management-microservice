package com.assessment.bank.rak.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}
	
	@Bean
	public RouteLocator testRoute(RouteLocatorBuilder builder) {
	    return builder.routes()
	        .route("hardcoded-test", r -> r.path("/get")
	            .uri("http://httpbin.org"))
	        .build();
	}

}
