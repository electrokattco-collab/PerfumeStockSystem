package com.perfumestock.backend.dto;

import jakarta.validation.constraints.*;

public class CustomerRequest {
    @NotBlank @Size(max = 100)
    private String name;
    private String phone;
    private String address;
    private String notes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
