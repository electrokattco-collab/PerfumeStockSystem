package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.entity.SaleItem;
import com.perfumestock.backend.entity.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SaleResponse {
    private Long id;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    private BigDecimal costOfGoodsSold;
    private BigDecimal profit;
    private PaymentType paymentType;
    private BigDecimal amountPaid;
    private BigDecimal amountOwing;
    private Long customerId;
    private String customerName;
    private List<SaleItemResponse> items;
    private LocalDateTime createdAt;

    public static SaleResponse from(Sale s) {
        SaleResponse r = new SaleResponse();
        r.id = s.getId();
        r.saleDate = s.getSaleDate();
        r.totalAmount = s.getTotalAmount();
        r.costOfGoodsSold = s.getCostOfGoodsSold();
        r.profit = s.getProfit();
        r.paymentType = s.getPaymentType();
        r.amountPaid = s.getAmountPaid();
        r.amountOwing = s.getAmountOwing();
        r.customerId = s.getCustomer() != null ? s.getCustomer().getId() : null;
        r.customerName = s.getCustomer() != null ? s.getCustomer().getName() : null;
        r.createdAt = s.getCreatedAt();
        if (s.getItems() != null) {
            r.items = s.getItems().stream().map(si -> {
                SaleItemResponse item = new SaleItemResponse();
                item.productId = si.getProduct().getId();
                item.productName = si.getProduct().getName();
                item.quantity = si.getQuantity();
                item.unitPrice = si.getUnitPrice();
                item.unitCost = si.getUnitCost();
                item.lineTotal = si.getLineTotal();
                return item;
            }).collect(Collectors.toList());
        }
        return r;
    }

    public Long getId() { return id; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getCostOfGoodsSold() { return costOfGoodsSold; }
    public BigDecimal getProfit() { return profit; }
    public PaymentType getPaymentType() { return paymentType; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public BigDecimal getAmountOwing() { return amountOwing; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public List<SaleItemResponse> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class SaleItemResponse {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal unitCost;
        private BigDecimal lineTotal;

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getUnitCost() { return unitCost; }
        public BigDecimal getLineTotal() { return lineTotal; }
    }
}
