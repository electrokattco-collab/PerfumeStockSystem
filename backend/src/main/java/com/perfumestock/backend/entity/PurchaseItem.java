package com.perfumestock.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull @Min(1)
    @Column(nullable = false)
    private int quantity;

    @NotNull
    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "is_combo_item", nullable = false)
    private boolean comboItem = false;

    public PurchaseItem() {}

    public PurchaseItem(Product product, int quantity, BigDecimal unitCost, boolean comboItem) {
        this.product = product;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.comboItem = comboItem;
    }

    public BigDecimal getLineTotal() { return unitCost.multiply(BigDecimal.valueOf(quantity)); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Purchase getPurchase() { return purchase; }
    public void setPurchase(Purchase purchase) { this.purchase = purchase; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public boolean isComboItem() { return comboItem; }
    public void setComboItem(boolean comboItem) { this.comboItem = comboItem; }
}
