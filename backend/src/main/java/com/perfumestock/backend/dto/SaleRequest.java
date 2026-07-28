package com.perfumestock.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaleRequest {
    private LocalDateTime saleDate;

    @NotNull
    private com.perfumestock.backend.entity.PaymentType paymentType;

    private BigDecimal amountPaid;
    private Long customerId;

    @NotEmpty
    @Valid
    private List<SaleItemRequest> items;

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public com.perfumestock.backend.entity.PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(com.perfumestock.backend.entity.PaymentType paymentType) { this.paymentType = paymentType; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }

    public static class SaleItemRequest {
        @NotNull
        private Long productId;

        @NotNull @Min(1)
        private int quantity;

        @NotNull @DecimalMin("0")
        private BigDecimal unitPrice;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}
