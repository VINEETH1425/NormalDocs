package com.storeconnect.order.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(OrderEvent event) {
        log.info("Publishing ORDER_PLACED event for order {} ({})", event.orderNumber(), event.orderId());
        try {
            kafkaTemplate.send("order-events", event.orderId(), event);
        } catch (Exception e) {
            log.warn("Could not publish ORDER_PLACED event to Kafka (broker may be offline): {}", e.getMessage());
        }
    }

    public void publishOrderCancelled(OrderEvent event) {
        log.info("Publishing ORDER_CANCELLED event for order {} ({})", event.orderNumber(), event.orderId());
        try {
            kafkaTemplate.send("order-events", event.orderId(), event);
        } catch (Exception e) {
            log.warn("Could not publish ORDER_CANCELLED event to Kafka: {}", e.getMessage());
        }
    }
}
