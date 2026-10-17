package com.storeconnect.order.repository;

import com.storeconnect.order.entity.Order;
import com.storeconnect.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Order> findByStoreIdOrderByCreatedAtDesc(String storeId);
    List<Order> findByStatus(OrderStatus status);
}
