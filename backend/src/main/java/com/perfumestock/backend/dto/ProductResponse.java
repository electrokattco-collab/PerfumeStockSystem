package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {
    private Long id;
    private String productId;
    private String name;
    private String category;
    private String size;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private boolean isLowStock;
    private String imageUrl;
    private String barcode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse() {}

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.id = product.getId();
        response.productId = product.getProductId();
        response.name = product.getName();
        response.category = product.getCategory();
        response.size = product.getSize();
        response.buyPrice = product.getBuyPrice();
        response.sellPrice = product.getSellPrice();
        response.stockQuantity = product.getStockQuantity();
        response.lowStockThreshold = product.getLowStockThreshold();
        response.isLowStock = product.isLowStock();
        response.imageUrl = product.getImageUrl();
        response.barcode = product.getBarcode();
        response.createdAt = product.getCreatedAt();
        response.updatedAt = product.getUpdatedAt();
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public BigDecimal getBuyPrice() { return buyPrice; }
    public void setBuyPrice(BigDecimal buyPrice) { this.buyPrice = buyPrice; }
    public BigDecimal getSellPrice() { return sellPrice; }
    public void setSellPrice(BigDecimal sellPrice) { this.sellPrice = sellPrice; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    public boolean getIsLowStock() { return isLowStock; }
    public void setIsLowStock(boolean isLowStock) { this.isLowStock = isLowStock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
