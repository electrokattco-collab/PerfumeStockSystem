package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Purchase;
import com.perfumestock.backend.entity.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Page<Purchase> findAllByOrderByPurchaseDateDesc(Pageable pageable);

    long countByStatusAndPurchaseDateBetween(PurchaseStatus status, LocalDateTime start, LocalDateTime end);

    long countByStatus(PurchaseStatus status);

    @Query("SELECT p FROM Purchase p WHERE p.purchaseDate BETWEEN :start AND :end ORDER BY p.purchaseDate DESC")
    Page<Purchase> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.totalAmount),0) FROM Purchase p WHERE p.status = :status AND p.purchaseDate BETWEEN :start AND :end")
    java.math.BigDecimal sumTotalByStatusBetween(@Param("status") PurchaseStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
