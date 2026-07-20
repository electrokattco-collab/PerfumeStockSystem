package com.perfumestock.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_receipt_items")
public class PurchaseReceiptItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    @JsonIgnore
    private PurchaseReceipt receipt;
    
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;
    
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    public PurchaseReceiptItem() {}
    public PurchaseReceiptItem(String productName, Integer quantity, BigDecimal unitCost) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = unitCost != null ? unitCost.multiply(BigDecimal.valueOf(quantity)) : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PurchaseReceipt getReceipt() { return receipt; }
    public void setReceipt(PurchaseReceipt receipt) { this.receipt = receipt; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
}
