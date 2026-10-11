package com.trading.orderservice.controller;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import com.trading.orderservice.dto.OrderRequest;
import com.trading.orderservice.dto.OrderResponse;
import com.trading.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeeOrder(@RequestHeader("X-User-Id") String userId,@RequestBody @Valid OrderRequest request) {
        request.setUserId(userId); // Overide the userId from the request header
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request).orElseThrow(() -> new RuntimeException("Failed to place order")));
    }

    @GetMapping("/{orderId}")
}
