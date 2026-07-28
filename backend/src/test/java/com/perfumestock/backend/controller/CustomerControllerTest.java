package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.CustomerBalanceResponse;
import com.perfumestock.backend.dto.CustomerLedgerEntry;
import com.perfumestock.backend.dto.CustomerStatementResponse;
import com.perfumestock.backend.entity.BusinessEventType;
import com.perfumestock.backend.exception.GlobalExceptionHandler;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.service.CustomerLedgerService;
import com.perfumestock.backend.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock private CustomerService customerService;
    @Mock private CustomerLedgerService customerLedgerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new CustomerController(customerService, customerLedgerService))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void ledgerEndpointReturnsLedgerEntries() throws Exception {
        CustomerLedgerEntry entry = new CustomerLedgerEntry();
        entry.setDate(LocalDateTime.of(2026, 7, 1, 10, 0));
        entry.setEventType(BusinessEventType.SALE_RECORDED.name());
        entry.setDescription("Sale #1 - Black 5");
        entry.setDebit(new BigDecimal("200.00"));
        entry.setCredit(BigDecimal.ZERO);
        entry.setRunningBalance(new BigDecimal("200.00"));
        entry.setReferenceId(1L);
        entry.setBusinessEventId(5L);
        entry.setSaleId(1L);

        when(customerLedgerService.getLedgerPage(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(entry), PageRequest.of(0, 50), 1));

        mockMvc.perform(get("/api/customers/1/ledger"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].eventType").value("SALE_RECORDED"))
            .andExpect(jsonPath("$.content[0].runningBalance").value(200.00));
    }

    @Test
    void balanceEndpointReturnsDerivedBalance() throws Exception {
        CustomerBalanceResponse balance = new CustomerBalanceResponse();
        balance.setOutstandingBalance(new BigDecimal("150.00"));
        balance.setTotalPurchases(new BigDecimal("200.00"));
        balance.setTotalPayments(new BigDecimal("50.00"));
        balance.setLastPaymentDate(LocalDateTime.of(2026, 7, 2, 12, 0));

        when(customerLedgerService.getBalance(1L)).thenReturn(balance);

        mockMvc.perform(get("/api/customers/1/balance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.outstandingBalance").value(150.00))
            .andExpect(jsonPath("$.totalPayments").value(50.00));
    }

    @Test
    void statementEndpointReturnsStatementPayload() throws Exception {
        CustomerStatementResponse statement = new CustomerStatementResponse();
        statement.setBusinessName("ArthurFord Gold Agent Business Manager");
        statement.setCustomerName("Ace");
        statement.setStatementPeriod("All time");
        statement.setOpeningBalance(BigDecimal.ZERO);
        statement.setClosingBalance(new BigDecimal("150.00"));
        statement.setTotalDebits(new BigDecimal("150.00"));
        statement.setTotalCredits(BigDecimal.ZERO);
        statement.setTransactionCount(0);
        statement.setGeneratedDate(LocalDateTime.of(2026, 7, 28, 12, 0));
        statement.setTransactions(List.of());

        when(customerLedgerService.getStatement(1L, null, null, null)).thenReturn(statement);

        mockMvc.perform(get("/api/customers/1/statement"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerName").value("Ace"))
            .andExpect(jsonPath("$.closingBalance").value(150.00));
    }

    @Test
    void unknownCustomerReturns404() throws Exception {
        when(customerLedgerService.getBalance(99L)).thenThrow(new ResourceNotFoundException("Customer", "id", 99L));

        mockMvc.perform(get("/api/customers/99/balance"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").exists());
    }
}
