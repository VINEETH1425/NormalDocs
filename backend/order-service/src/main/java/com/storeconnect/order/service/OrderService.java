package com.storeconnect.order.service;

import com.storeconnect.order.client.InventoryFeignClient;
import com.storeconnect.order.client.UserFeignClient;
import com.storeconnect.order.dto.*;
import com.storeconnect.order.entity.Order;
import com.storeconnect.order.entity.OrderItem;
import com.storeconnect.order.exception.BadRequestException;
import com.storeconnect.order.exception.ResourceNotFoundException;
import com.storeconnect.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "CREATED", "CONFIRMED", "PROCESSING", "READY_FOR_PICKUP", "COMPLETED", "DELIVERED", "CANCELLED"
    );

    private final OrderRepository orderRepository;
    private final UserFeignClient userFeignClient;
    private final InventoryFeignClient inventoryFeignClient;
    private final com.storeconnect.order.kafka.OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository,
                        UserFeignClient userFeignClient,
                        InventoryFeignClient inventoryFeignClient,
                        com.storeconnect.order.kafka.OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.userFeignClient = userFeignClient;
        this.inventoryFeignClient = inventoryFeignClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public OrderDTO checkout(String customerId, CheckoutRequest request) {
        CartDTO cart = userFeignClient.getCart(customerId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout: Cart is empty.");
        }

        String storeId = request.getStoreId();
        if (storeId == null || storeId.isBlank()) {
            storeId = cart.getStoreId() != null ? cart.getStoreId() : "STORE-GGN-01";
        }

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String orderNumber = "SC-" + System.currentTimeMillis();

        Order order = new Order(orderId,orderNumber,customerId,storeId,"CONFIRMED",request.getFulfillmentType() != null ? request.getFulfillmentType() : "STORE_PICKUP",BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (CartItemDTO item : cart.getItems()) {
            // Reserve stock via Feign call to store-inventory-service
            inventoryFeignClient.reserveStock(new StockReservationRequest(storeId, item.getProductId(), item.getQuantity()));

            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : new BigDecimal("100.00");
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);

            OrderItem orderItem = new OrderItem("OI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),order,item.getProductId(),item.getQuantity(),unitPrice,subtotal);
            items.add(orderItem);
        }

        order.setTotalAmount(total);
        order.setItems(items);
        Order saved = orderRepository.save(order);

        // Clear customer cart via Feign call to user-service
        userFeignClient.clearCart(customerId);

        // Publish Order Placed event to Kafka
        if (orderEventProducer != null) {
            orderEventProducer.publishOrderEvent("ORDER_PLACED", saved.getId(), saved.getOrderNumber(), saved.getCustomerId(), saved.getStoreId(), saved.getStatus(), saved.getTotalAmount());
        }

        return toDTO(saved);
    }

    public List<OrderDTO> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<OrderDTO> getOrdersByStore(String storeId) {
        return orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId).stream()
                .map(this::toDTO)
                .toList();
    }

    public OrderDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return toDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(String orderId, String newStatus) {
        String normalizedStatus = newStatus != null ? newStatus.trim().toUpperCase() : "";
        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new BadRequestException("Invalid order status: " + newStatus + ". Valid statuses are: " + VALID_STATUSES);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(normalizedStatus);
        Order updated = orderRepository.save(order);

        // Publish Order Status Updated event to Kafka
        if (orderEventProducer != null) {
            orderEventProducer.publishOrderEvent("STATUS_UPDATED", updated.getId(), updated.getOrderNumber(), updated.getCustomerId(), updated.getStoreId(), updated.getStatus(), updated.getTotalAmount());
        }

        return toDTO(updated);
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(i -> new OrderItemDTO(i.getId(), i.getProductId(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .toList();

        return new OrderDTO(order.getId(),order.getOrderNumber(),order.getCustomerId(),order.getStoreId(),order.getStatus(),order.getFulfillmentType(),order.getTotalAmount(),itemDTOs,order.getCreatedAt(),order.getUpdatedAt());
    }
}
