package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    Optional<Sale> findBySaleId(String saleId);
    
    List<Sale> findByProductNameContainingIgnoreCase(String productName);
    
    @Query("SELECT s FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<Sale> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Sale s WHERE s.createdAt >= :date")
    List<Sale> findSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT SUM(s.unitPrice * s.quantity) FROM Sale s WHERE s.createdAt >= :startOfDay")
    Double sumTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt >= :startOfDay")
    Long countTodaySales(@Param("startOfDay") LocalDateTime startOfDay);
}
