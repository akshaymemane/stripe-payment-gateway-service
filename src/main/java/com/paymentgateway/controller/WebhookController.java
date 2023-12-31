package com.paymentgateway.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestBody String sigHeader) {
        try {
            Event event = constructEvent(payload, sigHeader, webhookSecret);
            // Handle the event
            // You can delegate the actual handling to the PaymentService or handle it here

            return new ResponseEntity<>("Webhook handled successfully", HttpStatus.OK);
        } catch (SignatureVerificationException e) {
            return new ResponseEntity<>("Signature verification failed", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error handling webhook: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static Event constructEvent(String payload, String sigHeader, String endpointSecret)
            throws SignatureVerificationException {
        return Webhook.constructEvent(
                payload, sigHeader, endpointSecret
        );
    }
}
