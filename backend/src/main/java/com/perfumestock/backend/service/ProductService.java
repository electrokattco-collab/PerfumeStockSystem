package com.perfumestock.backend.service;

import com.perfumestock.backend.service.AuditLogService;
import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.dto.ProductRequest;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public PageResponse<Product> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return PageResponse.of(page);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public Product getProductByProductId(String productId) {
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        if (productRepository.existsByProductId(request.getProductId())) {
            throw new DuplicateResourceException("Product", "productId", request.getProductId());
        }

        Product product = new Product(
                request.getProductId(),
                request.getName(),
                request.getCategory(),
                request.getSize(),
                request.getBuyPrice(),
                request.getSellPrice(),
                request.getStockQuantity()
        );

        if (request.getLowStockThreshold() != null) {
            product.setLowStockThreshold(request.getLowStockThreshold());
        }

        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        if (request.getBarcode() != null) {
            product.setBarcode(request.getBarcode());
        }

        log.info("Created product: {} ({})", product.getName(), product.getProductId());
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);

        product.setProductId(request.getProductId());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setSize(request.getSize());
        product.setBuyPrice(request.getBuyPrice());
        product.setSellPrice(request.getSellPrice());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setImageUrl(request.getImageUrl());
        product.setBarcode(request.getBarcode());

        log.info("Updated product: {} (id: {})", product.getName(), id);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        log.info("Deleted product: {} (id: {})", product.getName(), id);
        productRepository.delete(product);
    }

    @Transactional
    public void clearAllProducts() {
        log.warn("Clearing all products");
        productRepository.deleteAll();
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public PageResponse<Product> searchProductsByName(String name, Pageable pageable) {
        Page<Product> page = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return PageResponse.of(page);
    }

    public List<Product> searchProductsByCategory(String category) {
        return productRepository.findByCategoryContainingIgnoreCase(category);
    }

    public PageResponse<Product> searchProductsByCategory(String category, Pageable pageable) {
        Page<Product> page = productRepository.findByCategoryContainingIgnoreCase(category, pageable);
        return PageResponse.of(page);
    }

    public PageResponse<Product> searchProducts(String name, String category, Pageable pageable) {
        Page<Product> page = productRepository.search(name, category, pageable);
        return PageResponse.of(page);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public PageResponse<Product> getLowStockProducts(Pageable pageable) {
        Page<Product> page = productRepository.findLowStockProducts(pageable);
        return PageResponse.of(page);
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }
}
