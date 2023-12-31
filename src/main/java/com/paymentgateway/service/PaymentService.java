package com.paymentgateway.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface PaymentService {
    PaymentIntent createPaymentIntent(double amount) throws StripeException;
}
