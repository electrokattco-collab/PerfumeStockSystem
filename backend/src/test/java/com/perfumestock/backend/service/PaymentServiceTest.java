package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.PaymentRequest;
import com.perfumestock.backend.dto.PaymentResponse;
import com.perfumestock.backend.entity.*;
import com.perfumestock.backend.exception.BusinessRuleException;
import com.perfumestock.backend.repository.CustomerRepository;
import com.perfumestock.backend.repository.PaymentRepository;
import com.perfumestock.backend.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepo;
    @Mock private CustomerRepository customerRepo;
    @Mock private SaleRepository saleRepo;
    @Mock private BusinessLedgerService ledgerService;

    @InjectMocks private PaymentService service;

    @Test
    void recordAppliesPartialPaymentWithoutTouchingCustomerBalance() throws Exception {
        Customer customer = customer(1L, "Ace");
        Sale sale = sale(10L, customer, new BigDecimal("80.00"));
        Payment saved = payment(99L, customer, sale, new BigDecimal("50.00"), LocalDateTime.of(2026, 7, 2, 12, 0));

        when(customerRepo.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(saleRepo.findById(sale.getId())).thenReturn(Optional.of(sale));
        when(paymentRepo.save(any(Payment.class))).thenReturn(saved);
        when(ledgerService.record(
            any(BusinessEventType.class),
            anyString(),
            anyLong(),
            any(),
            any(),
            any(),
            any(),
            anyString(),
            anyString()
        )).thenReturn(new BusinessEvent());

        PaymentRequest request = new PaymentRequest();
        request.setCustomerId(customer.getId());
        request.setSaleId(sale.getId());
        request.setAmount(new BigDecimal("50.00"));
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setNotes("Partial payment");

        PaymentResponse response = service.record(request);

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepo).save(saleCaptor.capture());
        assertThat(saleCaptor.getValue().getAmountPaid()).isEqualByComparingTo("50.00");
        assertThat(saleCaptor.getValue().getAmountOwing()).isEqualByComparingTo("30.00");

        verify(ledgerService, times(1)).record(
            eq(BusinessEventType.PAYMENT_RECEIVED),
            eq("PAYMENT"),
            eq(saved.getId()),
            eq(customer),
            isNull(),
            eq(saved.getAmount()),
            isNull(),
            any(),
            eq(saved.getPaymentMethod().name())
        );
        assertThat(response.getAmount()).isEqualByComparingTo("50.00");
        assertThat(response.getCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    void recordRejectsOverpaymentBeforeSavingAnything() {
        Customer customer = customer(1L, "Ace");
        Sale sale = sale(10L, customer, new BigDecimal("80.00"));

        when(customerRepo.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(saleRepo.findById(sale.getId())).thenReturn(Optional.of(sale));

        PaymentRequest request = new PaymentRequest();
        request.setCustomerId(customer.getId());
        request.setSaleId(sale.getId());
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod(PaymentMethod.CASH);

        assertThatThrownBy(() -> service.record(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("exceeds amount owing");

        verify(paymentRepo, never()).save(any());
        verify(ledgerService, never()).record(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private Customer customer(Long id, String name) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        return customer;
    }

    private Sale sale(Long id, Customer customer, BigDecimal owing) {
        Sale sale = new Sale();
        sale.setId(id);
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.of(2026, 7, 1, 10, 0));
        sale.setPaymentType(PaymentType.CREDIT);
        sale.setTotalAmount(new BigDecimal("80.00"));
        sale.setCostOfGoodsSold(new BigDecimal("40.00"));
        sale.setAmountPaid(new BigDecimal("0.00"));
        sale.setAmountOwing(owing);
        return sale;
    }

    private Payment payment(Long id, Customer customer, Sale sale, BigDecimal amount, LocalDateTime createdAt) throws Exception {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setCustomer(customer);
        payment.setSale(sale);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.CASH);
        setField(payment, "createdAt", createdAt);
        return payment;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
