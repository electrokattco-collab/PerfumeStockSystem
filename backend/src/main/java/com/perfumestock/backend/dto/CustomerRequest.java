package com.perfumestock.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CustomerRequest {
    @NotBlank @Size(max = 100)
    private String name;
    private String phone;
    private String address;
    private String notes;
    private BigDecimal amountOwing;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getAmountOwing() { return amountOwing; }
    public void setAmountOwing(BigDecimal amountOwing) { this.amountOwing = amountOwing; }
}
