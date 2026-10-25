package com.storeconnect.order.service;

import com.storeconnect.order.client.InventoryFeignClient;
import com.storeconnect.order.client.UserFeignClient;
import com.storeconnect.order.dto.*;
import com.storeconnect.order.entity.Order;
import com.storeconnect.order.entity.OrderItem;
import com.storeconnect.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserFeignClient userFeignClient;
    private final InventoryFeignClient inventoryFeignClient;

    public OrderService(OrderRepository orderRepository,UserFeignClient userFeignClient,InventoryFeignClient inventoryFeignClient) {
        this.orderRepository = orderRepository;
        this.userFeignClient = userFeignClient;
        this.inventoryFeignClient = inventoryFeignClient;
    }

    @Transactional
    public OrderDTO checkout(String customerId, CheckoutRequest request) {
        CartDTO cart = userFeignClient.getCart(customerId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout: Cart is empty.");
        }

        String storeId = request.getStoreId();
        if (storeId == null || storeId.isBlank()) {
            storeId = cart.getStoreId() != null ? cart.getStoreId() : "STORE-GGN-01";
        }

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String orderNumber = "SC-" + System.currentTimeMillis();

        Order order = new Order(orderId,orderNumber,customerId,storeId,"CREATED",request.getFulfillmentType() != null ? request.getFulfillmentType() : "STORE_PICKUP",BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (CartItemDTO item : cart.getItems()) {
            //  Reserve stock via Feign call to store-inventory-service
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

        //  Clear customer cart via Feign call to user-service
        userFeignClient.clearCart(customerId);

        return toDTO(saved);
    }

    public List<OrderDTO> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toDTO)
                .toList();
    }

    public OrderDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        return toDTO(order);
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(i -> new OrderItemDTO(i.getId(), i.getProductId(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .toList();

        return new OrderDTO(order.getId(),order.getOrderNumber(),order.getCustomerId(),order.getStoreId(),order.getStatus(),order.getFulfillmentType(),order.getTotalAmount(),itemDTOs,order.getCreatedAt(),order.getUpdatedAt());
    }
}
