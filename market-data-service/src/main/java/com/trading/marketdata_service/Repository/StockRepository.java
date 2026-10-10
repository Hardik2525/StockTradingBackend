package com.trading.marketdata_service.Repository;

import com.trading.marketdata_service.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {
    List<Stock> findByActiveTrue();
}
