package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.BusinessTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BusinessTransactionRepository extends JpaRepository<BusinessTransaction, Long> {
    List<BusinessTransaction> findByTransactionTypeAndTransactionDateAfter(String type, LocalDateTime since);
    @Query("SELECT bt FROM BusinessTransaction bt WHERE bt.transactionDate BETWEEN :start AND :end ORDER BY bt.transactionDate DESC")
    List<BusinessTransaction> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("SELECT SUM(bt.amount) FROM BusinessTransaction bt WHERE bt.transactionType = :type AND bt.transactionDate >= :since")
    Double sumByTypeSince(@Param("type") String type, @Param("since") LocalDateTime since);
    @Query("SELECT bt.transactionType, SUM(bt.amount) FROM BusinessTransaction bt WHERE bt.transactionDate >= :since GROUP BY bt.transactionType")
    List<Object[]> sumByTypeGrouped(@Param("since") LocalDateTime since);
}
