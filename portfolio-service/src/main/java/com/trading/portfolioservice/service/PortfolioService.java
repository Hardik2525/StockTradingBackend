package com.trading.portfolioservice.service;

import org.springframework.stereotype.Service;

import com.trading.portfolioservice.Repository.HoldingRepository;
import com.trading.portfolioservice.entity.Holding;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.util.HashMap;
import com.trading.portfolioservice.client.MarketDataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {
    private final HoldingRepository holdingRepository;
    private final MarketDataClient marketDataClient;

    public Map<String, Object> getPortfolio(String userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        List<Map<String, Object>> portfolioItems = new ArrayList<>();

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (Holding holding : holdings) {
          try{
            Map<String,Object> priceData = marketDataClient.getStockPrice(holding.getSymbol());
            BigDecimal currentPrice = new BigDecimal(priceData.get("price").toString());
            BigDecimal holdingValue = currentPrice.multiply(new BigDecimal(holding.getQuantity()));

            BigDecimal invested = holding.getTotalInvested();
            BigDecimal pnl = holdingValue.subtract(invested);

            BigDecimal pnlPercentage = pnl.divide(invested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);

            Map<String,Object> item = new HashMap<>();
            item.put("symbol", holding.getSymbol());
            item.put("companyName", holding.getCompanyName());
            item.put("quantity", holding.getQuantity());
            item.put("averageBuyPrice", holding.getAverageBuyPrice());
            item.put("totalInvested", holding.getTotalInvested());
            item.put("currentValue", holdingValue);
            item.put("pnl", pnl);
            item.put("pnlPercentage", pnlPercentage);
            item.put("isProfit", pnl.compareTo(BigDecimal.ZERO) > 0);
            portfolioItems.add(item);
            totalInvested = totalInvested.add(invested);
            totalCurrentValue = totalCurrentValue.add(holdingValue);
          }catch(Exception e){
            log.error("Error getting portfolio for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error getting portfolio for user " + userId, e);
          }
        }
        BigDecimal totalPnl = totalCurrentValue.subtract(totalInvested);
        BigDecimal totalPnlPercentage = totalPnl.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        Map<String,Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("holdings", holdings);
        response.put("portfolioItems", portfolioItems);
        response.put("totalInvested", totalInvested);
        response.put("totalCurrentValue", totalCurrentValue);
        response.put("totalPnl", totalPnl);
        response.put("totalPnlPercentage", totalPnlPercentage);
        response.put("isProfit", totalPnl.compareTo(BigDecimal.ZERO) > 0);
        return response;
    }
    
}
