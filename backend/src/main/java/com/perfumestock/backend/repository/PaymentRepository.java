package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Payment> findByCreatedAtGreaterThanEqual(LocalDateTime createdAt);
}
