package com.paymentgateway.service;

import com.paymentgateway.controller.WebhookController;
import com.paymentgateway.model.Payment;
import com.paymentgateway.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.apiKey}")
    private String stripeApiKey;

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public PaymentIntent createPaymentIntent(double amount) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", (int) (amount * 100));
        params.put("currency", "usd");

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Save payment details to your database
        Payment payment = new Payment();
        payment.setPaymentId(paymentIntent.getId());
        payment.setAmount(amount);
        paymentRepository.save(payment);

        return paymentIntent;
    }

    public void handleStripeWebhook(String payload, String sigHeader) throws StripeException {
        Event event = null;

        try {
            event = WebhookController.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            throw new RuntimeException("Webhook error: " + e.getMessage());
        }

        // Access the StripeObject directly without casting
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

        // Check if stripeObject is not null before further processing
        if (stripeObject != null) {
            // Handle different event types
            if ("payment_intent.succeeded".equals(event.getType())) {
                handlePaymentIntentSucceeded(stripeObject);
            } else {
                // Handle other event types as needed
            }
        } else {
            // Log or handle the case where stripeObject is null
        }
    }

    private void handlePaymentIntentSucceeded(StripeObject stripeObject) {
        if (stripeObject instanceof PaymentIntent) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            String paymentId = paymentIntent.getId();
            double amountReceived = paymentIntent.getAmountReceived() / 100.0; // Convert to dollars or your currency

            // Log the payment information
            System.out.println("Payment succeeded for payment ID: " + paymentId);
            System.out.println("Amount received: " + amountReceived);

            // Additional logic: Update your database, send confirmation emails, etc.
            // Example: paymentRepository.updatePaymentStatus(paymentId, "SUCCESS");
        } else {
            // Log or handle the case where the object is not a PaymentIntent
            System.err.println("Unexpected Stripe object type for payment_intent.succeeded event");
        }
    }
}
