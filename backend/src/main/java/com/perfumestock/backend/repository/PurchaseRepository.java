package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    
    Optional<Purchase> findByPurchaseId(String purchaseId);
    
    List<Purchase> findByProductId(Long productId);
    
    @Query("SELECT SUM(p.unitCost * p.quantity) FROM Purchase p")
    Double sumTotalInventoryCost();
    
    @Query("SELECT SUM(p.remainingQuantity) FROM Purchase p WHERE p.product.id = :productId")
    Integer sumRemainingQuantityByProductId(@Param("productId") Long productId);
}
