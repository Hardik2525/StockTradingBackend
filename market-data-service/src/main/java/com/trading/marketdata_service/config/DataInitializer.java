package com.trading.marketdata_service.config;

import com.trading.marketdata_service.Repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.trading.marketdata_service.entity.Stock;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StockRepository stockRepository;

    @Override
    public void run(String args) throws Exception{
        if(stockRepository.count()>0){
            log.info("Stocks already initialized - skipping");
            return;
        }

        log.info("Initializing stock data ....");
        List<Stock> stocks = List.of(

                // US stocks
                new Stock("AAPL", "Apple Inc",
                        BigDecimal.valueOf(189.50)),
                        "NASDAQ" , "USD",true
        ),
        new Stock("GOOGL", "Alphabet Inc",
                BigDecimal.valueOf(141.80)),
                "NASDAQ" , "USD",true
        ),
        new Stock("MSFT", "Microsoft Corp",
                BigDecimal.valueOf(378.90)),
                "NASDAQ" , "USD",true
        ),

        // INDIAN STCOKS
        new Stock("TCS", "Tata Consultancy Services",
                BigDecimal.valueOf(2850.00)),
                "NSE" , "INR",true
        ),
        new Stock("INFY", "Infosys  Services",
                BigDecimal.valueOf(1650.00)),
                "NSE" , "INR",true
        )

        stockRepository.saveAll(stocks);
        log.info("Initialized {}stocks successfully" ,stocks.size());
        );

    }

}
