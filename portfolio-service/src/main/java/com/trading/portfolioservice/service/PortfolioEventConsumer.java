package com.trading.portfolioservice.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.trading.portfolioservice.Repository.HoldingRepository;
import java.util.Map;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import java.math.RoundingMode;
import java.math.BigDecimal;
import com.trading.portfolioservice.entity.Holding;
@Service 
@Slf4j
@RequiredArgsConstructor
public class PortfolioEventConsumer {

    private final HoldingRepository holdingRepository;
    @KafkaListener(topics = "order-execution", groupId = "portfolio-service")
    public void consumeOrderExecuted(@Payload Map<String,Object> payload) {
        try {
            String userId = (String) payload.get("userId");
            String symbol = (String) payload.get("symbol");
            String type = (String) payload.get("type");
            int quantity = (int) payload.get("quantity");
            BigDecimal price = new BigDecimal((String) payload.get("price"));
            log.info("Updating portfolio for user {} {} {} * {} {}", userId, symbol, type, quantity, price);
           if("BUY".equals(type)){
            addHolding(userId, symbol, quantity, price);
           }
           else if("SELL".equals(type)){
            removeHolding(userId, symbol, quantity, price);
           }else{
            log.warn("Invalid order type: {}", type);
           }
           log.info("Portfolio updated for user {}", userId);
        }
        catch(Exception e){
            log.error("Error consuming order execution: {}", e.getMessage());
            throw new RuntimeException("Error consuming order execution", e);
        }
    }

    private void addHolding(String userId, String symbol, int quantity, BigDecimal price) {
        try {
            Holding holding = holdingRepository.findByUserIdAndSymbol(userId, symbol).orElse(null);
            if(holding == null){
                holding = new Holding();
                holding.setUserId(userId);
                holding.setSymbol(symbol);
                holding.setCompanyName(symbol.toUpperCase());
                holding.setQuantity(quantity);
                holding.setAverageBuyPrice(price);
                holding.setTotalInvested(price.multiply(new BigDecimal(quantity)));
                holdingRepository.save(holding);
            }else{
                int newQuantity = holding.getQuantity() + quantity;
                BigDecimal newInvested = holding.getTotalInvested().add(price.multiply(new BigDecimal(quantity)));
                BigDecimal newAveragePrice = newInvested.divide(new BigDecimal(newQuantity), 4, RoundingMode.HALF_UP);
                holding.setQuantity(newQuantity);
                holding.setTotalInvested(newInvested);
                holding.setAverageBuyPrice(newAveragePrice);
                holdingRepository.save(holding);
            }
            log.info("Holding added for user {} {} {} * {} {}", userId, symbol, quantity, price);
        }
        catch(Exception e){
            log.error("Error adding holding: {}", e.getMessage());
            throw new RuntimeException("Error adding holding", e);
        }
    }

    private void removeHolding(String userId, String symbol, int quantity, BigDecimal price) {
        try {
            Holding holding = holdingRepository.findByUserIdAndSymbol(userId, symbol).orElseThrow(() -> new RuntimeException("Holding not found for user to SELL"));
            
            if(holding.getQuantity() < quantity){
                log.warn("Insufficient quantity for user to SELL {} shares of {} {}", quantity, symbol, userId);
                throw new RuntimeException("Insufficient quantity for user to SELL");
            }
            int newQuantity = holding.getQuantity() - quantity;
            if(newQuantity == 0){
                holdingRepository.delete(holding);
                log.info("Holding sold for user {} {} {} * {} {}", userId, symbol, quantity, price);
                return;
            }else{
                BigDecimal newInvested = holding.getTotalInvested().subtract(price.multiply(new BigDecimal(quantity)));
                BigDecimal newAveragePrice = newInvested.divide(new BigDecimal(newQuantity), 4, RoundingMode.HALF_UP);
                holding.setQuantity(newQuantity);
                holding.setTotalInvested(newInvested);
                holding.setAverageBuyPrice(newAveragePrice);
                holdingRepository.save(holding);
                log.info("Holding sold for user {} {} {} * {} {}", userId, symbol, quantity, price);
            }
        }
        catch(Exception e){
            log.error("Error selling holding: {}", e.getMessage());
            throw new RuntimeException("Error selling holding", e);
        }
    }
}
