package com.trading.portfolioservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "holdings",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "symbol", nullable = false)
    private String symbol;
    @Column(name = "company_name", nullable = false)
    private String companyName;
    @Column(name = "quantity", nullable = false)
    private int quantity;
    @Column(name = "average_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal averageBuyPrice;
    @Column(name = "total_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInvested;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
