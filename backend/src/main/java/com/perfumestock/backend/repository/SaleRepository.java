package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.entity.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Page<Sale> findAllByOrderBySaleDateDesc(Pageable pageable);
    List<Sale> findByCustomerIdOrderBySaleDateAsc(Long customerId);
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Sale> findByIdIn(Collection<Long> ids);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate >= :start")
    long countSince(@Param("start") LocalDateTime start);

    @Query("SELECT COALESCE(SUM(s.totalAmount),0) FROM Sale s WHERE s.saleDate >= :start")
    BigDecimal sumTotalSince(@Param("start") LocalDateTime start);

    @Query("SELECT COALESCE(SUM(s.amountPaid),0) FROM Sale s WHERE s.saleDate >= :start")
    BigDecimal sumPaidSince(@Param("start") LocalDateTime start);

    @Query("SELECT COALESCE(SUM(s.costOfGoodsSold),0) FROM Sale s WHERE s.saleDate >= :start")
    BigDecimal sumCostSince(@Param("start") LocalDateTime start);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate >= :start AND s.paymentType = :type")
    long countByPaymentTypeSince(@Param("start") LocalDateTime start, @Param("type") PaymentType type);

    @Query("SELECT COALESCE(SUM(s.totalAmount),0) FROM Sale s WHERE s.saleDate >= :start AND s.paymentType = :type")
    BigDecimal sumTotalByPaymentTypeSince(@Param("start") LocalDateTime start, @Param("type") PaymentType type);

    @Query("SELECT COUNT(DISTINCT s.customer.id) FROM Sale s WHERE s.amountOwing > 0 AND s.customer IS NOT NULL AND s.saleDate < :cutoff")
    long countOverdueCustomers(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT COALESCE(SUM(s.amountOwing),0) FROM Sale s WHERE s.amountOwing > 0")
    BigDecimal totalOutstanding();

    @Query("SELECT COUNT(DISTINCT s.customer.id) FROM Sale s WHERE s.saleDate >= :start AND s.customer IS NOT NULL")
    long countCustomersSince(@Param("start") LocalDateTime start);

    @Query("SELECT COUNT(DISTINCT s.customer.id) FROM Sale s WHERE s.customer IS NOT NULL")
    long countDistinctCustomersWithSales();

    @Query("SELECT COALESCE(SUM(s.totalAmount),0) FROM Sale s WHERE s.customer IS NOT NULL")
    BigDecimal sumCustomerSalesTotal();

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :start AND :end ORDER BY s.saleDate DESC")
    Page<Sale> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    List<Sale> findTop10ByOrderBySaleDateDesc();

    @Query("SELECT si.product.name, SUM(si.quantity) as totalQty FROM SaleItem si GROUP BY si.product.name ORDER BY totalQty DESC")
    List<Object[]> topSellingProducts();
}
