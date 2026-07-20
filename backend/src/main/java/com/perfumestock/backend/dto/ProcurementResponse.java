package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Procurement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ProcurementResponse {
    private Long id;
    private String supplierName;
    private String supplierContact;
    private String invoiceNumber;
    private LocalDateTime purchaseDate;
    private String invoiceFilePath;
    private String invoiceType;
    private BigDecimal subtotal;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private String notes;
    private String uploadedBy;
    private String status;
    private List<ProcurementItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProcurementResponse() {}

    public static ProcurementResponse fromEntity(Procurement procurement) {
        ProcurementResponse response = new ProcurementResponse();
        response.id = procurement.getId();
        response.supplierName = procurement.getSupplierName();
        response.supplierContact = procurement.getSupplierContact();
        response.invoiceNumber = procurement.getInvoiceNumber();
        response.purchaseDate = procurement.getPurchaseDate();
        response.invoiceFilePath = procurement.getInvoiceFilePath();
        response.invoiceType = procurement.getInvoiceType();
        response.subtotal = procurement.getSubtotal();
        response.vatAmount = procurement.getVatAmount();
        response.totalAmount = procurement.getTotalAmount();
        response.notes = procurement.getNotes();
        response.uploadedBy = procurement.getUploadedBy();
        response.status = procurement.getStatus();
        response.items = procurement.getItems().stream()
                .map(ProcurementItemResponse::fromEntity)
                .collect(Collectors.toList());
        response.createdAt = procurement.getCreatedAt();
        response.updatedAt = procurement.getUpdatedAt();
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getInvoiceFilePath() { return invoiceFilePath; }
    public void setInvoiceFilePath(String invoiceFilePath) { this.invoiceFilePath = invoiceFilePath; }
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getVatAmount() { return vatAmount; }
    public void setVatAmount(BigDecimal vatAmount) { this.vatAmount = vatAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ProcurementItemResponse> getItems() { return items; }
    public void setItems(List<ProcurementItemResponse> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
