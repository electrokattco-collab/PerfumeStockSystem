package com.perfumestock.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_items")
public class ProcurementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procurement_id", nullable = false)
    @JsonIgnoreProperties({"items"})
    private Procurement procurement;

    @NotBlank
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "category", length = 50)
    private String category;

    @NotNull
    @Min(1)
    @Column(name = "quantity_purchased", nullable = false)
    private Integer quantityPurchased;

    @NotNull
    @Positive
    @Column(name = "buy_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal buyPrice;

    @Column(name = "suggested_selling_price", precision = 10, scale = 2)
    private BigDecimal suggestedSellingPrice;

    @Column(name = "expected_profit", precision = 10, scale = 2)
    private BigDecimal expectedProfit;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ProcurementItem() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public BigDecimal getLineTotal() {
        return buyPrice.multiply(BigDecimal.valueOf(quantityPurchased));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Procurement getProcurement() { return procurement; }
    public void setProcurement(Procurement procurement) { this.procurement = procurement; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
