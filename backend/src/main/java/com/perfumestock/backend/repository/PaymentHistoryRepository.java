package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.createdAt >= :since ORDER BY ph.createdAt DESC")
    List<PaymentHistory> findSince(@Param("since") LocalDateTime since);
}
