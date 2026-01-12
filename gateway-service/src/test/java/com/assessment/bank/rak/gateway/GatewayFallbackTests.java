package com.assessment.bank.rak.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
	    // Override the URLs to point to invalid addresses to force a failure
	    "STUDENT_SERVICE_URL=http://localhost:1234",
	    "PAYMENT_SERVICE_URL=http://localhost:1235",
	    "NOTIFICATION_SERVICE_URL=http://localhost:1236"
	})
@AutoConfigureWebTestClient
public class GatewayFallbackTests {

	@Autowired
    private WebTestClient webTestClient;

	@Test
    void whenStudentServiceDown_thenReturnsRefinedConnectError() {
        webTestClient.get()
                .uri("/api/students/123")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                // Verify the "refined" message logic for ConnectException
                .jsonPath("$.message").isEqualTo("The service is currently unreachable. Our engineers are working on it.")
                .jsonPath("$.errorCode").isEqualTo("SERVICE_UNAVAILABLE_GATEWAY")
                // Verify that FallbackHeaders captured the technical message
                .jsonPath("$.debug_info").value(org.hamcrest.Matchers.containsString("Connection refused"));
    }

    @Test
    void whenPaymentServiceDown_thenReturnsDetailedFallback() {
        webTestClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 100}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                // Verifying the debug info exists even if the user message is different
                .jsonPath("$.debug_info").exists()
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("unreachable"));
    }

    //@Test
    void whenNotificationServiceDown_thenReturnsNotificationFallback() {
        webTestClient.post()
                .uri("/api/notifications")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Payment was successful, but the receipt email could not be sent immediately.");
    }
	
}
