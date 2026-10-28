package com.storeconnect.order.service;

import com.storeconnect.order.client.InventoryFeignClient;
import com.storeconnect.order.client.UserFeignClient;
import com.storeconnect.order.dto.CartDTO;
import com.storeconnect.order.dto.CartItemDTO;
import com.storeconnect.order.dto.CheckoutRequest;
import com.storeconnect.order.dto.OrderDTO;
import com.storeconnect.order.entity.Order;
import com.storeconnect.order.entity.OrderItem;
import com.storeconnect.order.exception.BadRequestException;
import com.storeconnect.order.exception.ResourceNotFoundException;
import com.storeconnect.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserFeignClient userFeignClient;

    @Mock
    private InventoryFeignClient inventoryFeignClient;

    @Mock
    private com.storeconnect.order.kafka.OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(
                "ORD-TEST-01",
                "SC-1001",
                "CUST-001",
                "STORE-GGN-01",
                "CONFIRMED",
                "STORE_PICKUP",
                new BigDecimal("360.00")
        );
        order.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("checkout: empty cart throws BadRequestException")
    void testCheckout_EmptyCart() {
        when(userFeignClient.getCart("CUST-001")).thenReturn(new CartDTO("CART-01", "CUST-001", "STORE-GGN-01", List.of(), BigDecimal.ZERO));

        CheckoutRequest req = new CheckoutRequest("STORE-GGN-01", "STORE_PICKUP");
        assertThrows(BadRequestException.class, () -> orderService.checkout("CUST-001", req));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkout: valid cart successfully reserves inventory, creates order and clears cart")
    void testCheckout_Success() {
        CartItemDTO item = new CartItemDTO("CI-01", "PROD-APPLE-01", 2, new BigDecimal("180.00"), new BigDecimal("360.00"));
        CartDTO cart = new CartDTO("CART-01", "CUST-001", "STORE-GGN-01", List.of(item), new BigDecimal("360.00"));

        when(userFeignClient.getCart("CUST-001")).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            return o;
        });

        CheckoutRequest req = new CheckoutRequest("STORE-GGN-01", "STORE_PICKUP");
        OrderDTO result = orderService.checkout("CUST-001", req);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo("CUST-001");
        assertThat(result.getStoreId()).isEqualTo("STORE-GGN-01");
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("360.00"));

        verify(inventoryFeignClient, times(1)).reserveStock(any());
        verify(userFeignClient, times(1)).clearCart("CUST-001");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("updateOrderStatus: successfully transitions order status")
    void testUpdateOrderStatus_Success() {
        when(orderRepository.findById("ORD-TEST-01")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDTO updated = orderService.updateOrderStatus("ORD-TEST-01", "READY_FOR_PICKUP");

        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo("READY_FOR_PICKUP");
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("updateOrderStatus: invalid status throws BadRequestException")
    void testUpdateOrderStatus_InvalidStatus() {
        assertThrows(BadRequestException.class, () -> orderService.updateOrderStatus("ORD-TEST-01", "INVALID_STATUS"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrderById: throws ResourceNotFoundException when order does not exist")
    void testGetOrderById_NotFound() {
        when(orderRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById("NONEXISTENT"));
    }
}

