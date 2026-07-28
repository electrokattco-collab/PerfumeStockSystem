package com.perfumestock.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PaymentRequest {
    @NotNull
    private Long customerId;
    private Long saleId;
    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;
    private com.perfumestock.backend.entity.PaymentMethod paymentMethod = com.perfumestock.backend.entity.PaymentMethod.CASH;
    private String notes;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getSaleId() { return saleId; }
    public void setSaleId(Long saleId) { this.saleId = saleId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public com.perfumestock.backend.entity.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(com.perfumestock.backend.entity.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
