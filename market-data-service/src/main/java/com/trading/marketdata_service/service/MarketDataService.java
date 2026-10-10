package com.trading.marketdata_service.service;


import com.trading.marketdata_service.Repository.StockRepository;
import com.trading.marketdata_service.entity.Stock;
import com.trading.marketdata_service.dto.StockPrice;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.HashMap;
import org.springframework.kafka.core.KafkaTemplate;
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final StockRepository stockRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PRICE_KEY_PREFIX = "stock:price:";
    private final SimpMessagingTemplate simpMessagingTemplate;
    private static final String PRICE_UPDATED_TOPIC = "stock:price:updated";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, StockPrice> stockPrices = new ConcurrentHashMap<>();
    private final Random random = new Random();
    @PostConstruct
    public void initializePrices(){
        log.info("Loading Stock Data.....");

        List<Stock> stocks = stockRepository.findByActiveTrue();

        if(stocks.isEmpty()){
            log.info("No active stocks found");
            return;
        }
        for(Stock stock : stocks){
            StockPrice stockPrice = buildStockPrice(stock);
            stockPrices.put(stock.getSymbol(), stockPrice);
            log.info("Loaded stock {} at initial price {}", stock.getSymbol(), stock.getInitialPrice());
        }
        log.info("Stock Data loaded successfully with size {}", stockPrices.size());
    }

    private StockPrice buildStockPrice(Stock stock){
        return StockPrice.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .price(stock.getInitialPrice())
                .change(BigDecimal.ZERO)
                .changePercent(BigDecimal.ZERO)
                .high(stock.getInitialPrice())
                .low(stock.getInitialPrice())
                .open(stock.getInitialPrice())
                .volume(0)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Scheduled(fixedRateString = "${market.price-update-interval}")
    public void updatePrices(){
            if(stockPrices.isEmpty()){
                log.info("No stock prices found");
                return;
            }
            stockPrices.forEach((symbol, currentPrice) -> {
                BigDecimal oldPrice = currentPrice.getPrice();
                //+- 0.5 price movement
                double priceMovement = (random.nextDouble() - 0.5) * 0.5;

                BigDecimal priceChange = oldPrice.multiply(BigDecimal.valueOf(priceMovement/100)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal newPrice = oldPrice.add(priceChange).setScale(2, RoundingMode.HALF_UP);

                if(newPrice.compareTo(BigDecimal.ZERO) < 0){
                    newPrice = BigDecimal.valueOf(1);
                }

                BigDecimal changeAmount = newPrice.subtract(currentPrice.getOpen()).setScale(1, RoundingMode.HALF_UP);
                BigDecimal changePercent = changeAmount.divide(currentPrice.getOpen(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

                // update high/low

                BigDecimal newHigh = newPrice.compareTo(currentPrice.getHigh()) > 0 ? newPrice : currentPrice.getHigh();
                BigDecimal newLow = newPrice.compareTo(currentPrice.getLow()) < 0 ? newPrice : currentPrice.getLow();

                currentPrice.setPrice(newPrice);
                currentPrice.setChange(changeAmount);
                currentPrice.setChangePercent(changePercent);
                currentPrice.setHigh(newHigh);
                currentPrice.setLow(newLow);
                currentPrice.setTimestamp(LocalDateTime.now());
                log.info("Updated stock {} price to {}", symbol, newPrice);
                log.info("Change: {} ({})", changeAmount, changePercent);
                log.info("High: {}, Low: {}", newHigh, newLow);

                // Cache in Redis
                redisTemplate.opsForValue().set(PRICE_KEY_PREFIX + symbol, currentPrice);

                // Broadcast via websocket -> all clients
                simpMessagingTemplate.convertAndSend("/topic/prices" + symbol, currentPrice);

                // Publish to Kafka
                Map<String,Object> priceUpdatedEvent = new HashMap<>();
                priceUpdatedEvent.put("symbol", symbol);
                priceUpdatedEvent.put("price", newPrice);
                priceUpdatedEvent.put("timestamp", LocalDateTime.now().toString());
                kafkaTemplate.send(PRICE_UPDATED_TOPIC, priceUpdatedEvent);
            });
            log.info("Prices updated successfully");
        }

        public StockPrice getStockPrice(String symbol){
            Object cachedPrice = redisTemplate.opsForValue().get(PRICE_KEY_PREFIX + symbol);
            if(cachedPrice != null){
                return (StockPrice) cachedPrice;
            }
            //Fallback
            StockPrice price = stockPrices.get(symbol.toUpperCase());

            if(price == null){
                throw new RuntimeException("Stock price not found for symbol: " + symbol);
            }

            return price;
        }

        public List<StockPrice> getAllPrices(){
            return new ArrayList<>(stockPrices.values());
        }

        public List<Stock> getAllStocks(){
            return stockRepository.findByActiveTrue();
        }
    }
