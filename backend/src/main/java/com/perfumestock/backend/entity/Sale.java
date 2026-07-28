package com.perfumestock.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "cost_of_goods_sold", nullable = false, precision = 12, scale = 2)
    private BigDecimal costOfGoodsSold = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal profit;

    @Column(name = "payment_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentType paymentType;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "amount_owing", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountOwing = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Sale() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        profit = totalAmount.subtract(costOfGoodsSold);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getCostOfGoodsSold() { return costOfGoodsSold; }
    public void setCostOfGoodsSold(BigDecimal costOfGoodsSold) { this.costOfGoodsSold = costOfGoodsSold; }
    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }
    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getAmountOwing() { return amountOwing; }
    public void setAmountOwing(BigDecimal amountOwing) { this.amountOwing = amountOwing; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
