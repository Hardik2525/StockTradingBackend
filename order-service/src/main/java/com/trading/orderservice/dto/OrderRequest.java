package com.trading.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.trading.orderservice.entity.OrderType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String userId;
    @NotBlank(message = "Symbol is required")
    private String symbol;
    @NotBlank(message = "Type is required")
    private OrderType type;
    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;
}
