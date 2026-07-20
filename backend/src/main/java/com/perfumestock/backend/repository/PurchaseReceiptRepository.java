package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.PurchaseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceipt, Long> {
    List<PurchaseReceipt> findByStatus(String status);
    Page<PurchaseReceipt> findByStatus(String status, Pageable pageable);
    List<PurchaseReceipt> findBySupplierNameContainingIgnoreCase(String name);
}
