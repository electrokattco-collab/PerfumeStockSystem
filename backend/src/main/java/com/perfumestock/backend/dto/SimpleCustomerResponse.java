package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Customer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SimpleCustomerResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String notes;
    private BigDecimal amountOwing;
    private LocalDateTime createdAt;

    public static SimpleCustomerResponse from(Customer c) {
        SimpleCustomerResponse r = new SimpleCustomerResponse();
        r.id = c.getId();
        r.name = c.getName();
        r.phone = c.getPhone();
        r.address = c.getAddress();
        r.notes = c.getNotes();
        r.amountOwing = c.getAmountOwing();
        r.createdAt = c.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getNotes() { return notes; }
    public BigDecimal getAmountOwing() { return amountOwing; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
