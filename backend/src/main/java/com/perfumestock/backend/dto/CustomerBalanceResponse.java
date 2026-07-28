package com.perfumestock.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerBalanceResponse {
    private BigDecimal outstandingBalance = BigDecimal.ZERO;
    private BigDecimal totalPurchases = BigDecimal.ZERO;
    private BigDecimal totalPayments = BigDecimal.ZERO;
    private LocalDateTime lastPaymentDate;

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    public BigDecimal getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(BigDecimal totalPurchases) { this.totalPurchases = totalPurchases; }
    public BigDecimal getTotalPayments() { return totalPayments; }
    public void setTotalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; }
    public LocalDateTime getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(LocalDateTime lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
}
