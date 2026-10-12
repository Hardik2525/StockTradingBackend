package com.trading.orderservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trading.orderservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
