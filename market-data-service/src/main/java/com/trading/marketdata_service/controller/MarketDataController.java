package com.trading.marketdata_service.controller;

import com.trading.marketdata_service.controller.entity.Stock;
import com.trading.marketdata_service.dto.StockPrice;
import com.trading.marketdata_service.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/market")
@Slf4j
@RequiredArgsConstructor
public class MarketDataController {
    private  final MarketDataService marketDataService;

    @GetMapping("/stocks")
    public ResponseEntity<List<StockPrice>> getAllStocks(){
        return ResponseEntity.ok(marketDataService.getAllPrices());
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockPrice> getStockPrice(@PathVariable String symbol){
        return ResponseEntity.ok(marketDataService.getPrice(symbol.toUpperCase()))
    }

    @GetMapping("/stocks/list")
    public ResponseEntity<List<Stock>> getStockList(){
        return ResponseEntity.ok(marketDataService.getAllStocks());
    }



}
