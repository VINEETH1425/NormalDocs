package com.storeconnect.inventory.kafka;

import com.storeconnect.inventory.dto.StockReservationRequest;
import com.storeconnect.inventory.dto.StockValidateItem;
import com.storeconnect.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;

    public InventoryEventConsumer(InventoryService inventoryService, InventoryEventProducer inventoryEventProducer) {
        this.inventoryService = inventoryService;
        this.inventoryEventProducer = inventoryEventProducer;
    }

    @KafkaListener(topics = "order-events", groupId = "store-inventory-group")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for order {}", event.eventType(), event.orderId());

        if (event.items() == null || event.items().isEmpty()) {
            return;
        }

        List<StockValidateItem> items = event.items().stream()
                .map(i -> new StockValidateItem(i.productId(), i.quantity()))
                .toList();

        StockReservationRequest reservationRequest = new StockReservationRequest(
                event.orderId(),
                event.storeId(),
                items
        );

        if ("ORDER_PLACED".equalsIgnoreCase(event.eventType())) {
            try {
                inventoryService.reserveStock(reservationRequest);
                inventoryEventProducer.publishStockReserved(event.orderId(), event.storeId());
            } catch (Exception e) {
                log.error("Failed to reserve stock for order {}: {}", event.orderId(), e.getMessage());
                inventoryEventProducer.publishStockInsufficient(event.orderId(), event.storeId(), e.getMessage());
            }
        } else if ("ORDER_CANCELLED".equalsIgnoreCase(event.eventType())) {
            try {
                inventoryService.releaseStock(reservationRequest);
            } catch (Exception e) {
                log.error("Failed to release stock for order {}: {}", event.orderId(), e.getMessage());
            }
        }
    }
}
