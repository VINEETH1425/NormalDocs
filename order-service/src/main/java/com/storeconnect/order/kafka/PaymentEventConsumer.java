package com.storeconnect.order.kafka;

import com.storeconnect.order.entity.Order;
import com.storeconnect.order.entity.OrderStatus;
import com.storeconnect.order.feign.UserFeignClient;
import com.storeconnect.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderRepository orderRepository;
    private final UserFeignClient userFeignClient;
    private final OrderEventProducer orderEventProducer;

    public PaymentEventConsumer(OrderRepository orderRepository,
                                UserFeignClient userFeignClient,
                                OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.userFeignClient = userFeignClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentEvent(Map<String, Object> payload) {
        String eventType = (String) payload.get("eventType");
        String orderId = (String) payload.get("orderId");

        log.info("Received payment event: {} for order: {}", eventType, orderId);

        if (orderId == null || eventType == null) {
            return;
        }

        orderRepository.findById(orderId).ifPresent(order -> {
            if ("PAYMENT_SUCCESS".equalsIgnoreCase(eventType)) {
                if (order.getStatus() == OrderStatus.CREATED) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                    log.info("Order {} status updated to CONFIRMED following payment success", order.getOrderNumber());

                    // Clear customer cart
                    try {
                        userFeignClient.clearCart(order.getCustomerId());
                    } catch (Exception e) {
                        log.warn("Failed to clear cart via Feign for customer {}: {}", order.getCustomerId(), e.getMessage());
                    }
                }
            } else if ("PAYMENT_FAILED".equalsIgnoreCase(eventType)) {
                if (order.getStatus() == OrderStatus.CREATED) {
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                    log.warn("Order {} cancelled due to payment failure", order.getOrderNumber());

                    // Publish cancellation to release reserved inventory
                    OrderEvent cancelEvent = new OrderEvent(
                            "ORDER_CANCELLED",
                            order.getId(),
                            order.getOrderNumber(),
                            order.getCustomerId(),
                            order.getStoreId(),
                            order.getTotalAmount(),
                            order.getItems().stream()
                                    .map(i -> new OrderEvent.OrderItemPayload(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                                    .toList()
                    );
                    orderEventProducer.publishOrderCancelled(cancelEvent);
                }
            }
        });
    }
}
