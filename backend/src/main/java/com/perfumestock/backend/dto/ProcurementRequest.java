package com.perfumestock.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProcurementRequest {

    @NotBlank
    private String supplierName;

    private String supplierContact;

    private String invoiceNumber;

    @NotNull
    private LocalDateTime purchaseDate;

    private String invoiceFilePath;

    private String invoiceType;

    private BigDecimal vatAmount;

    private String notes;

    @NotNull
    @Size(min = 1, message = "At least one product item is required")
    @Valid
    private List<ProcurementItemRequest> items;

    public ProcurementRequest() {}

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
    public BigDecimal getVatAmount() { return vatAmount; }
    public void setVatAmount(BigDecimal vatAmount) { this.vatAmount = vatAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<ProcurementItemRequest> getItems() { return items; }
    public void setItems(List<ProcurementItemRequest> items) { this.items = items; }
}
