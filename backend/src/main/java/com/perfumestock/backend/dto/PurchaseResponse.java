package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Purchase;
import com.perfumestock.backend.entity.PurchaseSourceType;
import com.perfumestock.backend.entity.PurchaseStatus;
import com.perfumestock.backend.entity.PurchaseItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PurchaseResponse {
    private Long id;
    private LocalDateTime purchaseDate;
    private PurchaseSourceType sourceType;
    private PurchaseStatus status;
    private BigDecimal totalAmount;
    private String notes;
    private String receiptReference;
    private String ocrText;
    private BigDecimal ocrConfidence;
    private LocalDateTime confirmedAt;
    private String confirmedBy;
    private List<PurchaseItemResponse> items;
    private LocalDateTime createdAt;

    public static PurchaseResponse from(Purchase p) {
        PurchaseResponse r = new PurchaseResponse();
        r.id = p.getId();
        r.purchaseDate = p.getPurchaseDate();
        r.sourceType = p.getSourceType();
        r.status = p.getStatus();
        r.totalAmount = p.getTotalAmount();
        r.notes = p.getNotes();
        r.receiptReference = p.getReceiptReference();
        r.ocrText = p.getOcrText();
        r.ocrConfidence = p.getOcrConfidence();
        r.confirmedAt = p.getConfirmedAt();
        r.confirmedBy = p.getConfirmedBy();
        r.createdAt = p.getCreatedAt();
        if (p.getItems() != null) {
            r.items = p.getItems().stream().map(PurchaseItemResponse::from).collect(Collectors.toList());
        }
        return r;
    }

    public Long getId() { return id; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public PurchaseSourceType getSourceType() { return sourceType; }
    public PurchaseStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getNotes() { return notes; }
    public String getReceiptReference() { return receiptReference; }
    public String getOcrText() { return ocrText; }
    public BigDecimal getOcrConfidence() { return ocrConfidence; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public String getConfirmedBy() { return confirmedBy; }
    public List<PurchaseItemResponse> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class PurchaseItemResponse {
        private Long id;
        private Long productId;
        private String productCode;
        private String productName;
        private int quantity;
        private BigDecimal unitCost;
        private BigDecimal lineTotal;
        private boolean comboItem;

        public static PurchaseItemResponse from(PurchaseItem pi) {
            PurchaseItemResponse r = new PurchaseItemResponse();
            r.id = pi.getId();
            r.productId = pi.getProduct().getId();
            r.productCode = pi.getProduct().getProductCode();
            r.productName = pi.getProduct().getName();
            r.quantity = pi.getQuantity();
            r.unitCost = pi.getUnitCost();
            r.lineTotal = pi.getLineTotal();
            r.comboItem = pi.isComboItem();
            return r;
        }

        public Long getId() { return id; }
        public Long getProductId() { return productId; }
        public String getProductCode() { return productCode; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitCost() { return unitCost; }
        public BigDecimal getLineTotal() { return lineTotal; }
        public boolean isComboItem() { return comboItem; }
    }
}
