package com.automarket.marketplace.booking;

import java.math.BigDecimal;

public interface BookingPaymentGateway {

    record PaymentIntentResult(String paymentIntentId, String clientSecret) {}
    record RefundResult(String refundId, String status) {}

    PaymentIntentResult createPaymentIntent(BigDecimal amount, String currency, String bookingNumber, String description);
    boolean verifyWebhookSignature(String payload, String sigHeader);
    RefundResult refundPayment(String paymentIntentId, BigDecimal amount);
}

