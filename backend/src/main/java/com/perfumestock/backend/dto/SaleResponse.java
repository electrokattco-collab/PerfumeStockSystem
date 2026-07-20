package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.entity.SaleItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleResponse {
    private Long id;
    private String saleId;
    private String productName;
    private String category;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal costOfGoodsSold;
    private String customerName;
    private BigDecimal amountOwing;
    private boolean paid;
    private List<SaleItemResponse> items;
    private String recordedByUsername;
    private LocalDateTime createdAt;

    public SaleResponse() {}

    public static SaleResponse fromEntity(Sale sale) {
        SaleResponse response = new SaleResponse();
        response.id = sale.getId();
        response.saleId = sale.getSaleId();
        response.productName = sale.getProductName();
        response.category = sale.getCategory();
        response.quantity = sale.getQuantity();
        response.unitPrice = sale.getUnitPrice();
        response.totalAmount = sale.getTotalAmount();
        response.costOfGoodsSold = sale.getCostOfGoodsSold();
        response.customerName = sale.getCustomerName();
        response.amountOwing = sale.getAmountOwing();
        response.paid = sale.getPaid();
        response.recordedByUsername = sale.getRecordedBy() != null
                ? sale.getRecordedBy().getUsername() : null;
        response.createdAt = sale.getCreatedAt();

        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            response.items = new ArrayList<>();
            for (SaleItem item : sale.getItems()) {
                response.items.add(SaleItemResponse.fromEntity(item));
            }
        }

        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSaleId() { return saleId; }
    public void setSaleId(String saleId) { this.saleId = saleId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getCostOfGoodsSold() { return costOfGoodsSold; }
    public void setCostOfGoodsSold(BigDecimal costOfGoodsSold) { this.costOfGoodsSold = costOfGoodsSold; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public BigDecimal getAmountOwing() { return amountOwing; }
    public void setAmountOwing(BigDecimal amountOwing) { this.amountOwing = amountOwing; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public List<SaleItemResponse> getItems() { return items; }
    public void setItems(List<SaleItemResponse> items) { this.items = items; }
    public String getRecordedByUsername() { return recordedByUsername; }
    public void setRecordedByUsername(String recordedByUsername) { this.recordedByUsername = recordedByUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
