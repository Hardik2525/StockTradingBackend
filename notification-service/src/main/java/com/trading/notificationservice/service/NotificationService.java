package com.trading.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

        @KafkaListener(topics = "order.executed")
        public void consumeOrderExecuted(@Payload Map<String,Object> payload) {

            try{
                String userId = (String) payload.get("userId");
                String symbol = (String) payload.get("symbol");
                Object quantity = payload.get("quantity");
                Object price = payload.get("price");
                String type = (String) payload.get("type");
                Object totalAmount = payload.get("totalAmount");

                sendNotification(userId,
                    type.equals("BUY") ? "ORDER EXECUTED - BUY" : "ORDER EXECUTED - SELL",
                    String.format("You have %s %s for %s at %s", quantity, symbol, totalAmount, price));
                  
            }
            catch(Exception e){
                log.error("Error consuming order executed: {}", e.getMessage());
                throw new RuntimeException("Error consuming order executed", e);
            }


        }

        @KafkaListener(topics = "order.failed")
        public void consumeOrderFailed(@Payload Map<String,Object> payload) {
            try{
                String userId = (String) payload.get("userId");
                String symbol = (String) payload.get("symbol");
                Object quantity = payload.get("quantity");
                Object price = payload.get("price");
                String type = (String) payload.get("type");
                String reason = (String) payload.get("reason");

                sendNotification(userId,
                    "ORDER FAILED",
                    String.format("Your order for %s %s %s at %s failed because %s", quantity, symbol, type, price, reason));
            }
            catch(Exception e){
                log.error("Error consuming order failed: {}", e.getMessage());
                throw new RuntimeException("Error consuming order failed", e);
            }
        }

        @KafkaListener(topics = "user.registered")
        public void consumeUserRegistered(@Payload Map<String,Object> payload) {
            try{
                String userId = (String) payload.get("userId");
                String email = (String) payload.get("email");
                String firstname = (String) payload.get("name");
                Object initialBalance = payload.get("initialBalance");

                sendNotification(userId,
                    "USER REGISTERED",
                    String.format("Welcome %s to the trading platform with initial balance of %s", firstname, initialBalance));
            }
            catch(Exception e){
                log.error("Error consuming user registered: {}", e.getMessage());
                throw new RuntimeException("Error consuming user registered", e);
            }
        }
        private void sendNotification(String userId, String title, String message) {
            log.info("Sending notification to user {} with title {} and message {}", userId, title, message);
        }
}
