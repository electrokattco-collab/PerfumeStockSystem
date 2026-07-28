package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.ProductBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductBundleRepository extends JpaRepository<ProductBundle, Long> {
    List<ProductBundle> findByComboProductId(Long comboProductId);
}
