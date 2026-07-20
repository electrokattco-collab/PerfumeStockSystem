package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.ProcurementItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProcurementItemResponse {
    private Long id;
    private String productName;
    private String brand;
    private String category;
    private Integer quantityPurchased;
    private BigDecimal buyPrice;
    private BigDecimal suggestedSellingPrice;
    private BigDecimal expectedProfit;
    private BigDecimal lineTotal;
    private String barcode;
    private LocalDateTime expiryDate;
    private String batchNumber;

    public ProcurementItemResponse() {}

    public static ProcurementItemResponse fromEntity(ProcurementItem item) {
        ProcurementItemResponse response = new ProcurementItemResponse();
        response.id = item.getId();
        response.productName = item.getProductName();
        response.brand = item.getBrand();
        response.category = item.getCategory();
        response.quantityPurchased = item.getQuantityPurchased();
        response.buyPrice = item.getBuyPrice();
        response.suggestedSellingPrice = item.getSuggestedSellingPrice();
        response.expectedProfit = item.getExpectedProfit();
        response.lineTotal = item.getLineTotal();
        response.barcode = item.getBarcode();
        response.expiryDate = item.getExpiryDate();
        response.batchNumber = item.getBatchNumber();
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
}
