package com.perfumestock.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {
    @NotBlank @Size(max = 50)
    private String productCode;

    @NotBlank @Size(max = 100)
    private String name;

    @NotBlank @Size(max = 50)
    private String category;

    private boolean combo = false;

    @NotNull @DecimalMin("0")
    private BigDecimal buyPrice;

    @NotNull @DecimalMin("0")
    private BigDecimal sellPrice;

    private int lowStockThreshold = 5;

    private List<BundleItemRequest> bundleItems;

    // Getters and Setters
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isCombo() { return combo; }
    public void setCombo(boolean combo) { this.combo = combo; }
    public BigDecimal getBuyPrice() { return buyPrice; }
    public void setBuyPrice(BigDecimal buyPrice) { this.buyPrice = buyPrice; }
    public BigDecimal getSellPrice() { return sellPrice; }
    public void setSellPrice(BigDecimal sellPrice) { this.sellPrice = sellPrice; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    public List<BundleItemRequest> getBundleItems() { return bundleItems; }
    public void setBundleItems(List<BundleItemRequest> bundleItems) { this.bundleItems = bundleItems; }

    public static class BundleItemRequest {
        @NotNull
        private Long componentProductId;
        @NotNull @Min(1)
        private int quantity;

        public Long getComponentProductId() { return componentProductId; }
        public void setComponentProductId(Long componentProductId) { this.componentProductId = componentProductId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
