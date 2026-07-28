package com.perfumestock.backend.integration;

import com.perfumestock.backend.dto.CustomerBalanceResponse;
import com.perfumestock.backend.dto.PaymentRequest;
import com.perfumestock.backend.dto.PaymentResponse;
import com.perfumestock.backend.dto.SaleRequest;
import com.perfumestock.backend.dto.SaleResponse;
import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.entity.PaymentMethod;
import com.perfumestock.backend.entity.PaymentType;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.repository.BusinessEventRepository;
import com.perfumestock.backend.repository.CustomerRepository;
import com.perfumestock.backend.repository.ProductRepository;
import com.perfumestock.backend.service.CustomerLedgerService;
import com.perfumestock.backend.service.PaymentService;
import com.perfumestock.backend.service.SaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BusinessLedgerIntegrationTest {

    @Autowired private CustomerRepository customerRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private SaleService saleService;
    @Autowired private PaymentService paymentService;
    @Autowired private CustomerLedgerService customerLedgerService;
    @Autowired private BusinessEventRepository eventRepo;

    @Test
    void salePaymentAndReversalsRemainImmutableAndReconciled() {
        Customer customer = new Customer();
        customer.setName("Ace");
        customer.setPhone("0111111111");
        customer = customerRepo.save(customer);

        Product product = new Product();
        product.setProductCode("BLACK-5");
        product.setName("Black 5");
        product.setCategory("Perfume");
        product.setBuyPrice(new BigDecimal("40.00"));
        product.setSellPrice(new BigDecimal("100.00"));
        product.setStockQuantity(5);
        product.setLowStockThreshold(2);
        product = productRepo.save(product);

        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCustomerId(customer.getId());
        saleRequest.setPaymentType(PaymentType.CREDIT);
        saleRequest.setAmountPaid(BigDecimal.ZERO);
        saleRequest.setItems(List.of(saleItem(product.getId(), 1, new BigDecimal("100.00"))));

        SaleResponse sale = saleService.record(saleRequest);
        assertThat(sale.getAmountOwing()).isEqualByComparingTo("100.00");
        assertThat(productRepo.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(4);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setCustomerId(customer.getId());
        paymentRequest.setSaleId(sale.getId());
        paymentRequest.setAmount(new BigDecimal("40.00"));
        paymentRequest.setPaymentMethod(PaymentMethod.CASH);

        PaymentResponse payment = paymentService.record(paymentRequest);
        assertThat(payment.getAmount()).isEqualByComparingTo("40.00");

        CustomerBalanceResponse afterPayment = customerLedgerService.getBalance(customer.getId());
        assertThat(afterPayment.getOutstandingBalance()).isEqualByComparingTo("60.00");

        paymentService.reverse(payment.getId(), "Wrong amount captured");
        CustomerBalanceResponse afterPaymentReversal = customerLedgerService.getBalance(customer.getId());
        assertThat(afterPaymentReversal.getOutstandingBalance()).isEqualByComparingTo("100.00");

        saleService.reverse(sale.getId(), "Customer returned product");
        CustomerBalanceResponse afterSaleReversal = customerLedgerService.getBalance(customer.getId());
        assertThat(afterSaleReversal.getOutstandingBalance()).isEqualByComparingTo("0.00");
        assertThat(productRepo.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(5);

        assertThat(customerLedgerService.getStatement(customer.getId(), null, null, null).getTransactionCount())
            .isEqualTo(4);
        assertThat(eventRepo.findByCustomerId(customer.getId(), PageRequest.of(0, 20)).getTotalElements())
            .isEqualTo(4);
    }

    private SaleRequest.SaleItemRequest saleItem(Long productId, int quantity, BigDecimal unitPrice) {
        SaleRequest.SaleItemRequest item = new SaleRequest.SaleItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }
}
