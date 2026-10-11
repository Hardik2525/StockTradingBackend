package com.trading.orderservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String symbol;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    @Column(nullable = true)
    private String failureReason;
    @CreationTimestamp @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp @Column(nullable = false, updatable = true)
    private LocalDateTime updatedAt;
}