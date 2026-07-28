package com.perfumestock.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "product_bundles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"combo_product_id", "component_product_id"})
})
public class ProductBundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_product_id", nullable = false)
    private Product comboProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_product_id", nullable = false)
    private Product componentProduct;

    @NotNull @Min(1)
    @Column(nullable = false)
    private int quantity = 1;

    public ProductBundle() {}

    public ProductBundle(Product comboProduct, Product componentProduct, int quantity) {
        this.comboProduct = comboProduct;
        this.componentProduct = componentProduct;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getComboProduct() { return comboProduct; }
    public void setComboProduct(Product comboProduct) { this.comboProduct = comboProduct; }
    public Product getComponentProduct() { return componentProduct; }
    public void setComponentProduct(Product componentProduct) { this.componentProduct = componentProduct; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
