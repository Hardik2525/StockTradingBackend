package com.trading.orderservice.controller;

import com.trading.orderservice.entity.Order;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import com.trading.orderservice.dto.OrderRequest;
import com.trading.orderservice.dto.OrderResponse;
import com.trading.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestHeader("X-User-Id") String userId,@RequestBody @Valid OrderRequest request) {
        request.setUserId(userId); // Override the userId from the request header
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request).orElseThrow(() -> new RuntimeException("Failed to place order")));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId,
                                                  @RequestHeader("X-User_Id") String userId){
        Order order = orderService.getOrder(orderId);
        //Security check
        if(!order.getUserId().equals(userId)){
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrder(
            @RequestHeader("X-User-Id" ) String userId){
                return ResponseEntity.ok(orderService.getMyOrder(userId));
    }

}
