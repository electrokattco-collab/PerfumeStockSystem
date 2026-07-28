package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.CustomerBalanceResponse;
import com.perfumestock.backend.dto.CustomerLedgerEntry;
import com.perfumestock.backend.dto.CustomerStatementResponse;
import com.perfumestock.backend.entity.*;
import com.perfumestock.backend.repository.BusinessEventRepository;
import com.perfumestock.backend.repository.CustomerRepository;
import com.perfumestock.backend.repository.PaymentRepository;
import com.perfumestock.backend.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerLedgerServiceTest {

    @Mock private CustomerRepository customerRepo;
    @Mock private BusinessEventRepository eventRepo;
    @Mock private SaleRepository saleRepo;
    @Mock private PaymentRepository paymentRepo;

    @InjectMocks private CustomerLedgerService service;

    @Test
    void buildsRunningBalanceFromChronologicalLedgerEvents() throws Exception {
        Customer customer = customer(1L, "Ace");
        Product product = product(10L, "Black 5");
        Sale sale = sale(100L, customer, product, new BigDecimal("200.00"), BigDecimal.ZERO, new BigDecimal("200.00"));
        Payment payment = payment(200L, customer, new BigDecimal("50.00"), LocalDateTime.of(2026, 7, 2, 11, 0));

        BusinessEvent saleEvent = event(1L, BusinessEventType.SALE_RECORDED, "SALE", sale.getId(), customer, LocalDateTime.of(2026, 7, 1, 10, 0));
        BusinessEvent paymentEvent = event(2L, BusinessEventType.PAYMENT_RECEIVED, "PAYMENT", payment.getId(), customer, LocalDateTime.of(2026, 7, 2, 11, 0));

        when(customerRepo.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(eventRepo.findAll(org.mockito.ArgumentMatchers.any(Specification.class), org.mockito.ArgumentMatchers.any(Sort.class)))
            .thenReturn(List.of(saleEvent, paymentEvent));
        when(saleRepo.findByIdIn(org.mockito.ArgumentMatchers.anyCollection())).thenReturn(List.of(sale));
        when(paymentRepo.findAllById(org.mockito.ArgumentMatchers.anyCollection())).thenReturn(List.of(payment));
        when(saleRepo.findByCustomerIdOrderBySaleDateAsc(customer.getId())).thenReturn(List.of(sale));
        when(paymentRepo.findByCustomerIdOrderByCreatedAtAsc(customer.getId())).thenReturn(List.of(payment));
        when(paymentRepo.findByCustomerIdOrderByCreatedAtDesc(customer.getId())).thenReturn(List.of(payment));

        List<CustomerLedgerEntry> ledger = service.getLedger(customer.getId());
        assertThat(ledger).hasSize(2);
        assertThat(ledger.get(0).getRunningBalance()).isEqualByComparingTo("200.00");
        assertThat(ledger.get(1).getRunningBalance()).isEqualByComparingTo("150.00");
        assertThat(ledger.get(0).getDescription()).contains("Black 5");

        CustomerBalanceResponse balance = service.getBalance(customer.getId());
        assertThat(balance.getOutstandingBalance()).isEqualByComparingTo("150.00");
        assertThat(balance.getTotalPurchases()).isEqualByComparingTo("200.00");
        assertThat(balance.getTotalPayments()).isEqualByComparingTo("50.00");
        assertThat(balance.getLastPaymentDate()).isEqualTo(payment.getCreatedAt());

        CustomerStatementResponse statement = service.getStatement(customer.getId());
        assertThat(statement.getClosingBalance()).isEqualByComparingTo("150.00");
        assertThat(statement.getTransactions()).hasSize(2);
    }

    private Customer customer(Long id, String name) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        return customer;
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setProductCode("P-" + id);
        product.setCategory("Perfume");
        product.setBuyPrice(new BigDecimal("100.00"));
        product.setSellPrice(new BigDecimal("200.00"));
        return product;
    }

    private Sale sale(Long id, Customer customer, Product product, BigDecimal totalAmount, BigDecimal paid, BigDecimal owing) {
        Sale sale = new Sale();
        sale.setId(id);
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.of(2026, 7, 1, 10, 0));
        sale.setPaymentType(PaymentType.CREDIT);
        sale.setTotalAmount(totalAmount);
        sale.setCostOfGoodsSold(new BigDecimal("100.00"));
        sale.setAmountPaid(paid);
        sale.setAmountOwing(owing);
        SaleItem item = new SaleItem(product, 1, totalAmount, new BigDecimal("100.00"));
        item.setSale(sale);
        sale.getItems().add(item);
        return sale;
    }

    private Payment payment(Long id, Customer customer, BigDecimal amount, LocalDateTime createdAt) throws Exception {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setCustomer(customer);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.CASH);
        setField(payment, "createdAt", createdAt);
        return payment;
    }

    private BusinessEvent event(Long id, BusinessEventType type, String referenceType, Long referenceId,
                                Customer customer, LocalDateTime createdAt) throws Exception {
        BusinessEvent event = new BusinessEvent();
        event.setEventType(type);
        event.setReferenceType(referenceType);
        event.setReferenceId(referenceId);
        event.setCustomer(customer);
        event.setAmount(type == BusinessEventType.SALE_RECORDED ? new BigDecimal("200.00") : new BigDecimal("50.00"));
        setField(event, "id", id);
        setField(event, "createdAt", createdAt);
        return event;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
