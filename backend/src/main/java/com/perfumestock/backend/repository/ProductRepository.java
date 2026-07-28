package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);

    Page<Product> findByActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.productCode) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Product> search(String q, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStock();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.combo = false")
    List<Product> findNonComboActive();
}
