package com.trading.marketdata_service.service;


import com.trading.marketdata_service.Repository.StockRepository;
import com.trading.marketdata_service.controller.entity.Stock;
import com.trading.marketdata_service.dto.StockPrice;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final StockRepository stockRepository;

    private final Map<String, StockPrice> stockPrices = new ConcurrentHashMap<>();
    @PostConstruct
    public void initializePrices(){
        log.info("Loading Stock Data.....");

        List<Stock> stock = stockRepository.findByActiveTrue();

    }


}
