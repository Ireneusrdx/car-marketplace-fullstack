package com.automarket.marketplace.booking;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "stripe.secret-key", matchIfMissing = false)
public class StripeBookingPaymentGateway implements BookingPaymentGateway {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public PaymentIntentResult createPaymentIntent(BigDecimal amount, String currency, String bookingNumber, String description) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                .setCurrency(currency.toLowerCase())
                .putMetadata("bookingNumber", bookingNumber)
                .putMetadata("description", description)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true).build())
                .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return new PaymentIntentResult(intent.getId(), intent.getClientSecret());
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe payment intent", e); // Will be handled by ErrorHandler
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String sigHeader) {
        try {
            Webhook.constructEvent(payload, sigHeader, webhookSecret);
            return true;
        } catch (SignatureVerificationException e) {
            return false;
        }
    }

    @Override
    public RefundResult refundPayment(String paymentIntentId, BigDecimal amount) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                .build();
            Refund refund = Refund.create(params);
            return new RefundResult(refund.getId(), refund.getStatus());
        } catch (StripeException e) {
            throw new RuntimeException("Failed to refund Stripe payment", e);
        }
    }
}
