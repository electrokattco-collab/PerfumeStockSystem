package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByProductId(String productId);
    
    List<Product> findByCategoryContainingIgnoreCase(String category);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity BETWEEN :minStock AND :maxStock")
    List<Product> findByStockQuantityBetween(@Param("minStock") int minStock, @Param("maxStock") int maxStock);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();
    
    boolean existsByProductId(String productId);
}
