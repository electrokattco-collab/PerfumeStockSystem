package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Payment;
import com.perfumestock.backend.entity.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long saleId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String notes;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.id = p.getId();
        r.customerId = p.getCustomer().getId();
        r.customerName = p.getCustomer().getName();
        r.saleId = p.getSale() != null ? p.getSale().getId() : null;
        r.amount = p.getAmount();
        r.paymentMethod = p.getPaymentMethod();
        r.notes = p.getNotes();
        r.createdAt = p.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Long getSaleId() { return saleId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
