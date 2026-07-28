package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("SELECT COUNT(sm) FROM StockMovement sm WHERE sm.createdAt BETWEEN :start AND :end")
    long countBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
