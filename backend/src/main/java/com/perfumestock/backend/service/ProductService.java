package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.ProductRequest;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product getProductByProductId(String productId) {
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        if (productRepository.existsByProductId(request.getProductId())) {
            throw new RuntimeException("Product ID already exists: " + request.getProductId());
        }

        Product product = new Product(
                request.getProductId(),
                request.getName(),
                request.getCategory(),
                request.getSize(),
                request.getRetailPrice(),
                request.getRewardsPrice(),
                request.getGoldPrice(),
                request.getVipPrice(),
                request.getStockQuantity()
        );
        
        if (request.getLowStockThreshold() != null) {
            product.setLowStockThreshold(request.getLowStockThreshold());
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        
        product.setProductId(request.getProductId());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setSize(request.getSize());
        product.setRetailPrice(request.getRetailPrice());
        product.setRewardsPrice(request.getRewardsPrice());
        product.setGoldPrice(request.getGoldPrice());
        product.setVipPrice(request.getVipPrice());
        product.setLowStockThreshold(request.getLowStockThreshold());
        
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> searchProductsByCategory(String category) {
        return productRepository.findByCategoryContainingIgnoreCase(category);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    public List<Product> getProductsByStockRange(int minStock, int maxStock) {
        return productRepository.findByStockQuantityBetween(minStock, maxStock);
    }
}
