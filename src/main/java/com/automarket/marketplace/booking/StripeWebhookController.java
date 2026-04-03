package com.automarket.marketplace.booking;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final BookingPaymentGateway gateway;
    private final ProcessedEventRepository processedEventRepo;
    private final BookingService bookingService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader) {

        if (!gateway.verifyWebhookSignature(payload, sigHeader)) {
            log.warn("Invalid Stripe Webhook signature");
            return ResponseEntity.status(400).build();
        }

        try {
            Event event = Event.GSON.fromJson(payload, Event.class);

            if (processedEventRepo.existsById(event.getId())) {
                log.info("Stripe event {} already processed", event.getId());
                return ResponseEntity.ok().build();
            }

            if ("payment_intent.succeeded".equals(event.getType())) {
                // Use recommended deserializeUnsafe instead of deprecated getObject()
                com.stripe.model.StripeObject stripeObject = event.getDataObjectDeserializer()
                    .deserializeUnsafe();
                if (stripeObject instanceof PaymentIntent intent) {
                    String paymentIntentId = intent.getId();
                    String bookingNumber = intent.getMetadata().get("bookingNumber");

                    if (bookingNumber != null && !bookingNumber.isBlank()) {
                        bookingService.processSuccessfulPayment(bookingNumber, paymentIntentId);
                        log.info("Processed successful payment for booking {}", bookingNumber);
                    }
                } else {
                    // Fallback: parse raw JSON payload
                    com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
                    com.fasterxml.jackson.databind.JsonNode pi = root.path("data").path("object");
                    String paymentIntentId = pi.path("id").asText();
                    String bookingNumber = pi.path("metadata").path("bookingNumber").asText();

                    if (bookingNumber != null && !bookingNumber.isBlank()) {
                        bookingService.processSuccessfulPayment(bookingNumber, paymentIntentId);
                        log.info("Processed successful payment for booking {}", bookingNumber);
                    }
                }
            }

            processedEventRepo.save(new ProcessedEvent(event.getId(), LocalDateTime.now()));
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(500).build();
        }
    }
}
