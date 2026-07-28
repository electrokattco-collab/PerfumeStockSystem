package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.ProductBundle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ProductResponse {
    private Long id;
    private String productCode;
    private String name;
    private String category;
    private boolean combo;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private int stockQuantity;
    private int lowStockThreshold;
    private boolean lowStock;
    private boolean active;
    private List<BundleItemResponse> bundleItems;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.id = p.getId();
        r.productCode = p.getProductCode();
        r.name = p.getName();
        r.category = p.getCategory();
        r.combo = p.isCombo();
        r.buyPrice = p.getBuyPrice();
        r.sellPrice = p.getSellPrice();
        r.stockQuantity = p.getStockQuantity();
        r.lowStockThreshold = p.getLowStockThreshold();
        r.lowStock = p.isLowStock();
        r.active = p.isActive();
        r.createdAt = p.getCreatedAt();
        if (p.getBundleItems() != null) {
            r.bundleItems = p.getBundleItems().stream()
                .map(b -> new BundleItemResponse(b.getComponentProduct().getId(),
                    b.getComponentProduct().getProductCode(),
                    b.getComponentProduct().getName(),
                    b.getQuantity()))
                .collect(Collectors.toList());
        }
        return r;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getProductCode() { return productCode; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public boolean isCombo() { return combo; }
    public BigDecimal getBuyPrice() { return buyPrice; }
    public BigDecimal getSellPrice() { return sellPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public boolean isLowStock() { return lowStock; }
    public boolean isActive() { return active; }
    public List<BundleItemResponse> getBundleItems() { return bundleItems; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class BundleItemResponse {
        private Long productId;
        private String productCode;
        private String productName;
        private int quantity;

        public BundleItemResponse(Long productId, String productCode, String productName, int quantity) {
            this.productId = productId;
            this.productCode = productCode;
            this.productName = productName;
            this.quantity = quantity;
        }

        public Long getProductId() { return productId; }
        public String getProductCode() { return productCode; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
    }
}
