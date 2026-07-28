package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.entity.Payment;
import com.perfumestock.backend.entity.PaymentMethod;
import com.perfumestock.backend.entity.PaymentType;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.PurchaseStatus;
import com.perfumestock.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private SaleRepository saleRepo;
    @Mock private ProductRepository productRepo;
    @Mock private CustomerRepository customerRepo;
    @Mock private PurchaseRepository purchaseRepo;
    @Mock private StockMovementRepository stockMovementRepo;
    @Mock private BusinessEventRepository eventRepo;
    @Mock private CustomerLedgerService customerLedgerService;
    @Mock private PaymentRepository paymentRepo;

    @InjectMocks private ReportService service;

    @Test
    void dashboardUsesLedgerDerivedCustomerBalances() {
        Customer ace = customer(1L, "Ace", "111");
        Customer bee = customer(2L, "Bee", "222");
        Product product = product(10L, "Black 5");

        when(saleRepo.countSince(any(LocalDateTime.class))).thenReturn(7L);
        when(saleRepo.sumTotalSince(any(LocalDateTime.class))).thenReturn(new BigDecimal("300.00"));
        when(saleRepo.sumCostSince(any(LocalDateTime.class))).thenReturn(new BigDecimal("120.00"));
        when(saleRepo.sumTotalByPaymentTypeSince(any(LocalDateTime.class), any(PaymentType.class))).thenReturn(new BigDecimal("180.00"));
        when(saleRepo.sumPaidSince(any(LocalDateTime.class))).thenReturn(new BigDecimal("210.00"));
        when(saleRepo.countOverdueCustomers(any(LocalDateTime.class))).thenReturn(1L);
        when(saleRepo.sumCustomerSalesTotal()).thenReturn(new BigDecimal("900.00"));
        when(saleRepo.countDistinctCustomersWithSales()).thenReturn(3L);

        when(customerRepo.findAll()).thenReturn(List.of(ace, bee));
        when(customerLedgerService.getOutstandingBalance(ace.getId())).thenReturn(new BigDecimal("150.00"));
        when(customerLedgerService.getOutstandingBalance(bee.getId())).thenReturn(BigDecimal.ZERO);

        when(productRepo.findByActiveTrue(PageRequest.of(0, 10000))).thenReturn(new PageImpl<>(List.of(product)));
        when(purchaseRepo.countByStatusAndPurchaseDateBetween(any(PurchaseStatus.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(2L);
        when(purchaseRepo.sumTotalByStatusBetween(any(PurchaseStatus.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(new BigDecimal("500.00"));
        when(purchaseRepo.countByStatus(any(PurchaseStatus.class))).thenReturn(1L);
        when(stockMovementRepo.countBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(4L);
        when(paymentRepo.findByCreatedAtGreaterThanEqual(any(LocalDateTime.class))).thenReturn(List.of(payment(1L, ace)));
        when(eventRepo.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of());

        Map<String, Object> dashboard = service.dashboard();

        assertThat(dashboard.get("totalOutstanding")).isEqualTo(new BigDecimal("150.00"));
        assertThat(dashboard.get("totalCustomers")).isEqualTo(2);
        assertThat(dashboard.get("customersWithOutstandingBalances")).isEqualTo(1L);
        assertThat(dashboard.get("largestDebtor")).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) dashboard.get("largestDebtor")).get("id")).isEqualTo(1L);
        assertThat(dashboard.get("inventoryValue")).isEqualTo(new BigDecimal("100.00"));
        assertThat(dashboard.get("lowStockCount")).isEqualTo(1L);
        assertThat(dashboard.get("customersPaidThisMonth")).isEqualTo(1L);
        assertThat(dashboard.get("averageCustomerPurchaseValue")).isEqualTo(new BigDecimal("300.00"));
        assertThat(dashboard.get("monthProfit")).isEqualTo(new BigDecimal("180.00"));
    }

    @Test
    void debtReportIsBasedOnLedgerBalances() {
        Customer ace = customer(1L, "Ace", "111");
        Customer bee = customer(2L, "Bee", "222");

        when(customerRepo.findAll()).thenReturn(List.of(ace, bee));
        when(customerLedgerService.getOutstandingBalance(ace.getId())).thenReturn(new BigDecimal("150.00"));
        when(customerLedgerService.getOutstandingBalance(bee.getId())).thenReturn(BigDecimal.ZERO);

        Map<String, Object> debtReport = service.debtReport();

        assertThat(debtReport.get("totalOwing")).isEqualTo(new BigDecimal("150.00"));
        assertThat(debtReport.get("debtorCount")).isEqualTo(1);
        assertThat(((List<Map<String, Object>>) debtReport.get("debtors"))).hasSize(1);
    }

    private Customer customer(Long id, String name, String phone) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setPhone(phone);
        return customer;
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setProductCode("P-" + id);
        product.setCategory("Perfume");
        product.setBuyPrice(new BigDecimal("25.00"));
        product.setSellPrice(new BigDecimal("50.00"));
        product.setStockQuantity(4);
        product.setLowStockThreshold(5);
        return product;
    }

    private Payment payment(Long id, Customer customer) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setCustomer(customer);
        payment.setAmount(new BigDecimal("210.00"));
        payment.setPaymentMethod(PaymentMethod.CASH);
        return payment;
    }
}
