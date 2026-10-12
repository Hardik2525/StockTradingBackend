package com.trading.portfolioservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trading.portfolioservice.entity.Holding;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoldingRepository extends JpaRepository<Holding, String> {
    @Query("SELECT h FROM Holding h WHERE h.userId = :userId")
    List<Holding> findByUserId(@Param("userId") String userId);

    @Query("SELECT h FROM Holding h WHERE h.userId = :userId AND h.symbol = :symbol")
    Holding findByUserIdAndSymbol(@Param("userId") String userId, @Param("symbol") String symbol);
}
