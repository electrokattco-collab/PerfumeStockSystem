package com.perfumestock.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProcurementItemRequest {

    @NotBlank
    private String productName;

    private String brand;

    private String category;

    @NotNull
    @Min(1)
    private Integer quantityPurchased;

    @NotNull
    @Positive
    private BigDecimal buyPrice;

    private BigDecimal suggestedSellingPrice;

    private BigDecimal expectedProfit;

    private String barcode;

    private LocalDateTime expiryDate;

    private String batchNumber;

    public ProcurementItemRequest() {}

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantityPurchased() { return quantityPurchased; }
    public void setQuantityPurchased(Integer quantityPurchased) { this.quantityPurchased = quantityPurchased; }
    public BigDecimal getBuyPrice() { return buyPrice; }
    public void setBuyPrice(BigDecimal buyPrice) { this.buyPrice = buyPrice; }
    public BigDecimal getSuggestedSellingPrice() { return suggestedSellingPrice; }
    public void setSuggestedSellingPrice(BigDecimal suggestedSellingPrice) { this.suggestedSellingPrice = suggestedSellingPrice; }
    public BigDecimal getExpectedProfit() { return expectedProfit; }
    public void setExpectedProfit(BigDecimal expectedProfit) { this.expectedProfit = expectedProfit; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
}
