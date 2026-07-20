package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.SaleItem;

import java.math.BigDecimal;

public class SaleItemResponse {
    private Long id;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public SaleItemResponse() {}

    public static SaleItemResponse fromEntity(SaleItem item) {
        SaleItemResponse response = new SaleItemResponse();
        response.id = item.getId();
        response.productName = item.getProductName();
        response.quantity = item.getQuantity();
        response.unitPrice = item.getUnitPrice();
        response.lineTotal = item.getLineTotal();
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
