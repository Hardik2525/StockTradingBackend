package com.trading.orderservice.service;

import com.trading.orderservice.client.MarketDataClient;
import com.trading.orderservice.dto.OrderRequest;
import com.trading.orderservice.entity.Order;
import com.trading.orderservice.entity.OrderStatus;
import com.trading.orderservice.entity.OrderType;
import com.trading.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.trading.orderservice.client.UserServiceClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MarketDataClient marketDataClient;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    private static final String ORDER_EXECUTED_TOPIC = "order.executed";
    private static final String ORDER_FAILED_TOPIC = "order.failed";


    // Place a buy or sell order
    // 1. Get current price from market
    // 2. Calculate total amount
    // 3. Execute
    // 4. Publish order Event from Kafka

    public Order placeOrder(OrderRequest request) {
        log.info("Placing {} order : {} x {} for user: {}",
                request.getType(), request.getQuantity(), request.getSymbol(), request.getUserId());

        // Step 1.
        Map<String, Object> priceData = marketDataClient.getStockPrice(request.getSymbol());

        BigDecimal currentPrice = new BigDecimal(priceData.get("price").toString());

        BigDecimal totalAmount = currentPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .userId(request.getUserId())
                .symbol(request.getSymbol())
                .type(request.getType())
                .quantity(request.getQuantity())
                .price(currentPrice)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        Order savedorder = orderRepository.save(order);

        try {
            if(request.getType() == OrderType.BUY){
                userServiceClient.deductFunds(request.getUserId() , totalAmount);
            }
            else{
                userServiceClient.creditFunds(request.getUserId(),totalAmount);
            }

            savedorder.setStatus(OrderStatus.EXECUTED);
            savedorder.setCreatedAt(LocalDateTime.now());
            orderRepository.save(savedorder);

            publishOrderEvent(ORDER_EXECUTED_TOPIC,savedorder , null);

        }catch (Exception e){
            savedorder.setStatus(OrderStatus.FAILED);
            savedorder.setFailureReason(e.getMessage());
            orderRepository.save(savedorder);

            publishOrderEvent(ORDER_FAILED_TOPIC,savedorder , e.getMessage());

        }
        return savedorder;
    }

    private void publishOrderEvent(String topic, Order order , String reason){
        Map<String, Object>  orderEvent = new HashMap<>();
        orderEvent.put("orderId" , order.getId());
        orderEvent.put("symbol",order.getSymbol());
        orderEvent.put("userId",order.getUserId());
        orderEvent.put("type",order.getType());
        orderEvent.put("quantity",order.getQuantity());
        orderEvent.put("price",order.getPrice());
        orderEvent.put("totalAmount",order.getTotalAmount());
        orderEvent.put("status",order.getStatus());
        if(reason!=null) orderEvent.put("reason",reason);
        kafkaTemplate.send(topic,order.getId(),orderEvent);
    }

    public Order getOrder(String orderId){
        return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getUserOrders(String userId){
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
