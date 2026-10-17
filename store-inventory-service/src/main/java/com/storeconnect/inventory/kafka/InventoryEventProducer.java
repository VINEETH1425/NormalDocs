package com.storeconnect.inventory.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStockReserved(String orderId, String storeId) {
        log.info("Publishing STOCK_RESERVED event for order {}", orderId);
        try {
            kafkaTemplate.send("inventory-events", orderId, Map.of(
                    "eventType", "STOCK_RESERVED",
                    "orderId", orderId,
                    "storeId", storeId
            ));
        } catch (Exception e) {
            log.warn("Could not publish STOCK_RESERVED to Kafka (broker may be offline): {}", e.getMessage());
        }
    }

    public void publishStockInsufficient(String orderId, String storeId, String reason) {
        log.warn("Publishing STOCK_INSUFFICIENT event for order {}: {}", orderId, reason);
        try {
            kafkaTemplate.send("inventory-events", orderId, Map.of(
                    "eventType", "STOCK_INSUFFICIENT",
                    "orderId", orderId,
                    "storeId", storeId,
                    "reason", reason
            ));
        } catch (Exception e) {
            log.warn("Could not publish STOCK_INSUFFICIENT to Kafka: {}", e.getMessage());
        }
    }
}
