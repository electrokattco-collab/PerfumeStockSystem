package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Sale;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SaleRequest {
    
    @NotBlank
    private String productId;
    
    @NotNull
    @Min(1)
    private Integer quantity;
    
    @NotNull
    private Sale.CustomerTier customerTier;
    
    public SaleRequest() {
    }
    
    // Getters and Setters
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Sale.CustomerTier getCustomerTier() {
        return customerTier;
    }
    
    public void setCustomerTier(Sale.CustomerTier customerTier) {
        this.customerTier = customerTier;
    }
}
