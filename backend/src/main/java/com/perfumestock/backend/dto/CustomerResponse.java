package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Customer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String notes;
    private BigDecimal outstandingBalance;
    private LocalDateTime createdAt;

    public static CustomerResponse from(Customer c, BigDecimal outstandingBalance) {
        CustomerResponse r = new CustomerResponse();
        r.id = c.getId();
        r.name = c.getName();
        r.phone = c.getPhone();
        r.address = c.getAddress();
        r.notes = c.getNotes();
        r.outstandingBalance = outstandingBalance;
        r.createdAt = c.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getNotes() { return notes; }
    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
