package com.storeconnect.order.service;

import com.storeconnect.order.dto.CheckoutRequest;
import com.storeconnect.order.dto.OrderDTO;
import com.storeconnect.order.dto.OrderItemDTO;
import com.storeconnect.order.dto.OrderStatusUpdateRequest;
import com.storeconnect.order.dto.feign.*;
import com.storeconnect.order.entity.FulfillmentType;
import com.storeconnect.order.entity.Order;
import com.storeconnect.order.entity.OrderItem;
import com.storeconnect.order.entity.OrderStatus;
import com.storeconnect.order.exception.*;
import com.storeconnect.order.feign.InventoryFeignClient;
import com.storeconnect.order.feign.UserFeignClient;
import com.storeconnect.order.kafka.OrderEvent;
import com.storeconnect.order.kafka.OrderEventProducer;
import com.storeconnect.order.repository.OrderItemRepository;
import com.storeconnect.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserFeignClient userFeignClient;
    private final InventoryFeignClient inventoryFeignClient;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserFeignClient userFeignClient,
                        InventoryFeignClient inventoryFeignClient,
                        OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userFeignClient = userFeignClient;
        this.inventoryFeignClient = inventoryFeignClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public OrderDTO checkout(String customerId, CheckoutRequest request) {
        log.info("Starting checkout for customer: {}", customerId);

        // 1. Fetch active shopping cart
        CartDTO cart;
        try {
            cart = userFeignClient.getCart(customerId);
        } catch (Exception e) {
            log.error("Failed to fetch cart for customer {}: {}", customerId, e.getMessage());
            throw new CheckoutFailedException("Unable to retrieve cart: " + e.getMessage());
        }

        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new CheckoutFailedException("Checkout failed: Your shopping cart is empty");
        }

        // 2. Determine fulfillment store
        String storeId = (request.storeId() != null && !request.storeId().isBlank())
                ? request.storeId().trim()
                : (cart.storeId() != null && !cart.storeId().isBlank() ? cart.storeId() : "STORE-GGN-01");

        // 3. Validate stock availability at the target store
        List<StockValidateItem> stockItems = cart.items().stream()
                .map(item -> new StockValidateItem(item.productId(), item.quantity()))
                .toList();

        try {
            StockValidateResponse validation = inventoryFeignClient.validateStock(
                    new StockValidateRequest(storeId, stockItems)
            );

            if (validation != null && !validation.isAvailable()) {
                String issues = validation.issues().stream()
                        .map(i -> String.format("Product %s: requested %d, available %d", i.productId(), i.requestedQuantity(), i.availableQuantity()))
                        .collect(Collectors.joining("; "));
                throw new CheckoutFailedException("Stock unavailable: " + issues);
            }
        } catch (CheckoutFailedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not validate stock via Feign (continuing with checkout): {}", e.getMessage());
        }

        // 4. Create and persist the Order
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNumber = "ORD-" + datePrefix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        FulfillmentType fulfillmentType = request.fulfillmentType() != null
                ? request.fulfillmentType()
                : FulfillmentType.HOME_DELIVERY;

        Order order = Order.builder()
                .id(orderId)
                .orderNumber(orderNumber)
                .customerId(customerId)
                .storeId(storeId)
                .status(OrderStatus.CREATED)
                .fulfillmentType(fulfillmentType)
                .totalAmount(cart.totalAmount() != null ? cart.totalAmount() : BigDecimal.ZERO)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 5. Create Order Items and associate with parent Order (cascaded by JPA)
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartDTO.CartItemDTO item : cart.items()) {
            OrderItem orderItem = OrderItem.builder()
                    .id("OITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .order(order)
                    .productId(item.productId())
                    .quantity(item.quantity())
                    .unitPrice(item.unitPrice())
                    .subtotal(item.subtotal())
                    .build();
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully: {} ({}) with total: {}", orderNumber, orderId, savedOrder.getTotalAmount());

        // 6. Synchronous stock reservation & cart cleanup
        try {
            inventoryFeignClient.reserveStock(new StockReservationRequest(
                    savedOrder.getId(),
                    savedOrder.getStoreId(),
                    stockItems
            ));
            log.info("Stock reserved directly via Feign for order {}", savedOrder.getId());
        } catch (Exception e) {
            log.warn("Could not synchronously reserve stock via Feign: {}", e.getMessage());
        }

        try {
            userFeignClient.clearCart(customerId);
            log.info("Cart cleared for customer {} after checkout", customerId);
        } catch (Exception e) {
            log.warn("Could not clear cart via Feign: {}", e.getMessage());
        }

        // 7. Publish ORDER_PLACED event to Kafka
        List<OrderEvent.OrderItemPayload> eventItems = orderItems.stream()
                .map(i -> new OrderEvent.OrderItemPayload(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        OrderEvent placedEvent = new OrderEvent(
                "ORDER_PLACED",
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getCustomerId(),
                savedOrder.getStoreId(),
                savedOrder.getTotalAmount(),
                eventItems
        );
        orderEventProducer.publishOrderPlaced(placedEvent);

        return toDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(String orderId, String requestingCustomerId, String userRole) {
        Order order = orderRepository.findById(orderId)
                .orElseGet(() -> orderRepository.findByOrderNumber(orderId)
                        .orElseThrow(() -> new OrderNotFoundException(orderId)));

        boolean isStaff = "ROLE_ADMIN".equalsIgnoreCase(userRole) || "ROLE_STORE_OPERATOR".equalsIgnoreCase(userRole);
        if (!isStaff && !order.getCustomerId().equals(requestingCustomerId)) {
            throw new UnauthorizedOrderAccessException(orderId);
        }

        return toDTO(order);
    }

    @Transactional
    public OrderDTO cancelOrder(String orderId, String customerId, String userRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        boolean isStaff = "ROLE_ADMIN".equalsIgnoreCase(userRole) || "ROLE_STORE_OPERATOR".equalsIgnoreCase(userRole);
        if (!isStaff && !order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedOrderAccessException(orderId);
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new OrderNotCancellableException(orderId, order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        log.info("Order cancelled: {}", order.getOrderNumber());

        // Publish cancellation to Kafka
        OrderEvent cancelEvent = new OrderEvent(
                "ORDER_CANCELLED",
                saved.getId(),
                saved.getOrderNumber(),
                saved.getCustomerId(),
                saved.getStoreId(),
                saved.getTotalAmount(),
                saved.getItems().stream()
                        .map(i -> new OrderEvent.OrderItemPayload(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                        .toList()
        );
        orderEventProducer.publishOrderCancelled(cancelEvent);

        return toDTO(saved);
    }

    @Transactional
    public OrderDTO updateStatus(String orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getStatus().canTransitionTo(request.status())) {
            throw new InvalidStatusTransitionException(order.getStatus(), request.status());
        }

        order.setStatus(request.status());
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        log.info("Updated order {} status to {}", saved.getOrderNumber(), saved.getStatus());

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStore(String storeId) {
        return orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId).stream()
                .map(this::toDTO)
                .toList();
    }

    public OrderDTO toDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(i -> new OrderItemDTO(i.getId(), i.getProductId(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .toList();

        return new OrderDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStoreId(),
                order.getStatus(),
                order.getFulfillmentType(),
                order.getTotalAmount(),
                itemDTOs,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
