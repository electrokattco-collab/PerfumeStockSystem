package com.perfumestock.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PurchaseRequest {
    private LocalDateTime purchaseDate;
    private com.perfumestock.backend.entity.PurchaseSourceType sourceType;
    private String receiptReference;
    private String ocrText;
    private BigDecimal ocrConfidence;
    private String notes;

    @NotEmpty
    @Valid
    private List<PurchaseItemRequest> items;

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
    public com.perfumestock.backend.entity.PurchaseSourceType getSourceType() { return sourceType; }
    public void setSourceType(com.perfumestock.backend.entity.PurchaseSourceType sourceType) { this.sourceType = sourceType; }
    public String getReceiptReference() { return receiptReference; }
    public void setReceiptReference(String receiptReference) { this.receiptReference = receiptReference; }
    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }
    public BigDecimal getOcrConfidence() { return ocrConfidence; }
    public void setOcrConfidence(BigDecimal ocrConfidence) { this.ocrConfidence = ocrConfidence; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<PurchaseItemRequest> getItems() { return items; }
    public void setItems(List<PurchaseItemRequest> items) { this.items = items; }

    public static class PurchaseItemRequest {
        @NotNull
        private Long productId;

        @NotNull @Min(1)
        private int quantity;

        @NotNull @DecimalMin("0")
        private BigDecimal unitCost;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    }
}
