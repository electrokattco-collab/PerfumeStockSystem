package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
    List<StockMovement> findByMovementTypeAndCreatedAtAfter(String type, LocalDateTime since);
    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt BETWEEN :start AND :end ORDER BY sm.createdAt DESC")
    List<StockMovement> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
