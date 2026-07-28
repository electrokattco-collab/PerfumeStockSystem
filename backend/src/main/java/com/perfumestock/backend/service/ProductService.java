package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.ProductRequest;
import com.perfumestock.backend.dto.ProductResponse;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.ProductBundle;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.ProductBundleRepository;
import com.perfumestock.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final ProductBundleRepository bundleRepo;

    public ProductService(ProductRepository productRepo, ProductBundleRepository bundleRepo) {
        this.productRepo = productRepo;
        this.bundleRepo = bundleRepo;
    }

    public Page<ProductResponse> list(Pageable pageable) {
        return productRepo.findByActiveTrue(pageable).map(ProductResponse::from);
    }

    public Page<ProductResponse> search(String q, Pageable pageable) {
        return productRepo.search(q, pageable).map(ProductResponse::from);
    }

    public ProductResponse getById(Long id) {
        Product p = productRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ProductResponse.from(p);
    }

    public List<ProductResponse> getLowStock() {
        return productRepo.findLowStock().stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> getNonComboActive() {
        return productRepo.findNonComboActive().stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        if (productRepo.existsByProductCode(req.getProductCode())) {
            throw new DuplicateResourceException("Product", "productCode", req.getProductCode());
        }

        Product p = new Product();
        p.setProductCode(req.getProductCode());
        p.setName(req.getName());
        p.setCategory(req.getCategory());
        p.setCombo(req.isCombo());
        p.setBuyPrice(req.getBuyPrice());
        p.setSellPrice(req.getSellPrice());
        p.setLowStockThreshold(req.getLowStockThreshold());
        p = productRepo.save(p);

        if (req.isCombo() && req.getBundleItems() != null) {
            for (ProductRequest.BundleItemRequest bi : req.getBundleItems()) {
                Product component = productRepo.findById(bi.getComponentProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Component product", "id", bi.getComponentProductId()));
                ProductBundle bundle = new ProductBundle(p, component, bi.getQuantity());
                bundleRepo.save(bundle);
            }
        }

        return ProductResponse.from(p);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product p = productRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        p.setName(req.getName());
        p.setCategory(req.getCategory());
        p.setBuyPrice(req.getBuyPrice());
        p.setSellPrice(req.getSellPrice());
        p.setLowStockThreshold(req.getLowStockThreshold());

        if (req.isCombo() && req.getBundleItems() != null) {
            List<ProductBundle> existing = bundleRepo.findByComboProductId(id);
            for (ProductBundle b : existing) {
                bundleRepo.delete(b);
            }
            for (ProductRequest.BundleItemRequest bi : req.getBundleItems()) {
                Product component = productRepo.findById(bi.getComponentProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Component product", "id", bi.getComponentProductId()));
                ProductBundle bundle = new ProductBundle(p, component, bi.getQuantity());
                bundleRepo.save(bundle);
            }
        }

        return ProductResponse.from(productRepo.save(p));
    }

    @Transactional
    public void delete(Long id) {
        Product p = productRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        p.setActive(false);
        productRepo.save(p);
    }
}
