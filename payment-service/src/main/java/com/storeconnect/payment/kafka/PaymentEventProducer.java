package com.storeconnect.payment.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentSuccess(String paymentId, String orderId, BigDecimal amount) {
        log.info("Publishing PAYMENT_SUCCESS event for order {} (payment: {})", orderId, paymentId);
        try {
            kafkaTemplate.send("payment-events", orderId, Map.of(
                    "eventType", "PAYMENT_SUCCESS",
                    "paymentId", paymentId,
                    "orderId", orderId,
                    "amount", amount,
                    "status", "SUCCESS"
            ));
        } catch (Exception e) {
            log.warn("Could not publish PAYMENT_SUCCESS to Kafka (broker may be offline): {}", e.getMessage());
        }
    }

    public void publishPaymentFailed(String paymentId, String orderId, BigDecimal amount, String failureReason) {
        log.warn("Publishing PAYMENT_FAILED event for order {}: {}", orderId, failureReason);
        try {
            kafkaTemplate.send("payment-events", orderId, Map.of(
                    "eventType", "PAYMENT_FAILED",
                    "paymentId", paymentId,
                    "orderId", orderId,
                    "amount", amount,
                    "status", "FAILED",
                    "failureReason", failureReason
            ));
        } catch (Exception e) {
            log.warn("Could not publish PAYMENT_FAILED to Kafka: {}", e.getMessage());
        }
    }
}
