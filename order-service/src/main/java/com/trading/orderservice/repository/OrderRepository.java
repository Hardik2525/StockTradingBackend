package com.trading.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trading.orderservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
    
}
