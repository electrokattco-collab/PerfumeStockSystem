package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.BusinessEvent;
import com.perfumestock.backend.entity.BusinessEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BusinessEventRepository extends JpaRepository<BusinessEvent, Long>, JpaSpecificationExecutor<BusinessEvent> {
    List<BusinessEvent> findTop20ByOrderByCreatedAtDesc();
    List<BusinessEvent> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
    Page<BusinessEvent> findByCustomerId(Long customerId, Pageable pageable);
    boolean existsByEventTypeAndReferenceTypeAndReferenceId(BusinessEventType eventType, String referenceType, Long referenceId);
    BusinessEvent findFirstByEventTypeAndReferenceTypeAndReferenceId(BusinessEventType eventType, String referenceType, Long referenceId);

    @Query("SELECT COUNT(e) FROM BusinessEvent e WHERE e.eventType = :type AND e.createdAt >= :start AND e.createdAt <= :end")
    long countByTypeBetween(@Param("type") BusinessEventType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM BusinessEvent e WHERE e.eventType = :type AND e.createdAt >= :start AND e.createdAt <= :end")
    BigDecimal sumAmountByTypeBetween(@Param("type") BusinessEventType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(e.quantity), 0) FROM BusinessEvent e WHERE e.eventType = :type AND e.createdAt >= :start AND e.createdAt <= :end")
    long sumQuantityByTypeBetween(@Param("type") BusinessEventType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN e.eventType IN ('SALE_RECORDED', 'PURCHASE_REVERSED', 'PAYMENT_REVERSED')
                    THEN e.amount
                WHEN e.eventType IN ('PAYMENT_RECEIVED', 'SALE_REVERSED', 'PURCHASE_RECORDED')
                    THEN -e.amount
                ELSE 0
            END
        ), 0)
        FROM BusinessEvent e
        WHERE e.customer.id = :customerId
          AND (:startDate IS NULL OR e.createdAt >= :startDate)
          AND (:endDate IS NULL OR e.createdAt <= :endDate)
          AND (:eventType IS NULL OR e.eventType = :eventType)
    """)
    BigDecimal calculateCustomerBalanceBefore(
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("eventType") BusinessEventType eventType
    );

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN e.eventType IN ('SALE_RECORDED', 'PURCHASE_REVERSED', 'PAYMENT_REVERSED')
                    THEN e.amount
                ELSE 0
            END
        ), 0)
        FROM BusinessEvent e
        WHERE e.customer.id = :customerId
          AND (:startDate IS NULL OR e.createdAt >= :startDate)
          AND (:endDate IS NULL OR e.createdAt <= :endDate)
          AND (:eventType IS NULL OR e.eventType = :eventType)
    """)
    BigDecimal sumDebitsForCustomer(
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("eventType") BusinessEventType eventType
    );

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN e.eventType IN ('PAYMENT_RECEIVED', 'SALE_REVERSED', 'PURCHASE_RECORDED')
                    THEN e.amount
                ELSE 0
            END
        ), 0)
        FROM BusinessEvent e
        WHERE e.customer.id = :customerId
          AND (:startDate IS NULL OR e.createdAt >= :startDate)
          AND (:endDate IS NULL OR e.createdAt <= :endDate)
          AND (:eventType IS NULL OR e.eventType = :eventType)
    """)
    BigDecimal sumCreditsForCustomer(
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("eventType") BusinessEventType eventType
    );
}
