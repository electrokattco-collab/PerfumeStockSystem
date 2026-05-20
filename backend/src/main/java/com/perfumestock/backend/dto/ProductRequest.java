package com.perfumestock.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ProductRequest {
    
    @NotBlank
    private String productId;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String category;
    
    private String size;
    
    @NotNull
    @Positive
    private BigDecimal retailPrice;
    
    @NotNull
    @Positive
    private BigDecimal rewardsPrice;
    
    @NotNull
    @Positive
    private BigDecimal goldPrice;
    
    @NotNull
    @Positive
    private BigDecimal vipPrice;
    
    @Min(0)
    private Integer stockQuantity = 0;
    
    private Integer lowStockThreshold = 5;
    
    public ProductRequest() {
    }
    
    // Getters and Setters
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public BigDecimal getRetailPrice() {
        return retailPrice;
    }
    
    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice;
    }
    
    public BigDecimal getRewardsPrice() {
        return rewardsPrice;
    }
    
    public void setRewardsPrice(BigDecimal rewardsPrice) {
        this.rewardsPrice = rewardsPrice;
    }
    
    public BigDecimal getGoldPrice() {
        return goldPrice;
    }
    
    public void setGoldPrice(BigDecimal goldPrice) {
        this.goldPrice = goldPrice;
    }
    
    public BigDecimal getVipPrice() {
        return vipPrice;
    }
    
    public void setVipPrice(BigDecimal vipPrice) {
        this.vipPrice = vipPrice;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }
    
    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
}
