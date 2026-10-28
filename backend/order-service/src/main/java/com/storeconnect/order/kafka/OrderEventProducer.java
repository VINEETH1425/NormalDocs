package com.storeconnect.order.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(@Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderEvent(String eventType, String orderId, String orderNumber, String customerId, String storeId, String status, BigDecimal totalAmount) {
        if (kafkaTemplate == null) {
            log.info("[Kafka Fallback] KafkaTemplate not configured. Gracefully bypassed {} event for order: {}", eventType, orderId);
            return;
        }

        try {
            OrderEvent event = new OrderEvent(
                    eventType,
                    orderId,
                    orderNumber,
                    customerId,
                    storeId,
                    status,
                    totalAmount,
                    LocalDateTime.now()
            );
            kafkaTemplate.send(TOPIC, orderId, event);
            log.info("[Kafka Producer] Dispatched {} event for order: {} to topic: {}", eventType, orderId, TOPIC);
        } catch (Exception e) {
            log.warn("[Kafka Producer] Could not publish {} event to topic {} (broker offline): {}", eventType, TOPIC, e.getMessage());
        }
    }
}

