package com.perfumestock.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CustomerStatementResponse {
    private String businessName;
    private String customerName;
    private String statementPeriod;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal openingBalance = BigDecimal.ZERO;
    private BigDecimal closingBalance = BigDecimal.ZERO;
    private BigDecimal totalDebits = BigDecimal.ZERO;
    private BigDecimal totalCredits = BigDecimal.ZERO;
    private long transactionCount;
    private LocalDateTime generatedDate;
    private List<CustomerLedgerEntry> transactions;

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getStatementPeriod() { return statementPeriod; }
    public void setStatementPeriod(String statementPeriod) { this.statementPeriod = statementPeriod; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }
    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal closingBalance) { this.closingBalance = closingBalance; }
    public BigDecimal getTotalDebits() { return totalDebits; }
    public void setTotalDebits(BigDecimal totalDebits) { this.totalDebits = totalDebits; }
    public BigDecimal getTotalCredits() { return totalCredits; }
    public void setTotalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; }
    public long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(long transactionCount) { this.transactionCount = transactionCount; }
    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }
    public List<CustomerLedgerEntry> getTransactions() { return transactions; }
    public void setTransactions(List<CustomerLedgerEntry> transactions) { this.transactions = transactions; }
}
