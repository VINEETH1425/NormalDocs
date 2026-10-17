package com.storeconnect.payment.kafka;

import com.storeconnect.payment.dto.PaymentRequest;
import com.storeconnect.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final PaymentService paymentService;

    public OrderEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderEvent(Map<String, Object> payload) {
        String eventType = (String) payload.get("eventType");
        String orderId = (String) payload.get("orderId");
        Object totalObj = payload.get("totalAmount");

        log.info("Payment service received order event: {} for order: {}", eventType, orderId);

        if ("ORDER_PLACED".equalsIgnoreCase(eventType) && orderId != null) {
            BigDecimal amount = BigDecimal.ZERO;
            if (totalObj instanceof Number num) {
                amount = BigDecimal.valueOf(num.doubleValue());
            } else if (totalObj instanceof String str) {
                amount = new BigDecimal(str);
            }

            try {
                paymentService.processPayment(new PaymentRequest(orderId, amount, "CREDIT_CARD"));
            } catch (Exception e) {
                log.error("Failed to process payment automatically for order {}: {}", orderId, e.getMessage());
            }
        }
    }
}
