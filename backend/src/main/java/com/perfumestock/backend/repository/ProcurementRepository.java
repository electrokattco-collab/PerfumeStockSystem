package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Procurement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcurementRepository extends JpaRepository<Procurement, Long> {

    Optional<Procurement> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Page<Procurement> findBySupplierNameContainingIgnoreCase(String supplierName, Pageable pageable);

    @Query("SELECT p FROM Procurement p WHERE " +
           "(:supplierName IS NULL OR LOWER(p.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%'))) AND " +
           "(:invoiceNumber IS NULL OR LOWER(p.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%'))) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Procurement> search(@Param("supplierName") String supplierName,
                             @Param("invoiceNumber") String invoiceNumber,
                             @Param("status") String status,
                             Pageable pageable);

    @Query("SELECT COUNT(p) FROM Procurement p WHERE p.purchaseDate >= :start AND p.purchaseDate < :end")
    long countByPurchaseDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Procurement p WHERE p.purchaseDate >= :start AND p.purchaseDate < :end")
    java.math.BigDecimal sumTotalAmountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT p.supplierName) FROM Procurement p")
    long countDistinctSuppliers();

    List<Procurement> findTop10ByOrderByCreatedAtDesc();
}
