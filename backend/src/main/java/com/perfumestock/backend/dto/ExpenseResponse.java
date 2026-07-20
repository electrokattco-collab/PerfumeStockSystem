package com.perfumestock.backend.dto;

import com.perfumestock.backend.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpenseResponse {
    private Long id;
    private String category;
    private String description;
    private BigDecimal amount;
    private LocalDateTime expenseDate;
    private LocalDateTime createdAt;

    public ExpenseResponse() {}

    public static ExpenseResponse fromEntity(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.id = expense.getId();
        response.category = expense.getCategory();
        response.description = expense.getDescription();
        response.amount = expense.getAmount();
        response.expenseDate = expense.getExpenseDate();
        response.createdAt = expense.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDateTime expenseDate) { this.expenseDate = expenseDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
