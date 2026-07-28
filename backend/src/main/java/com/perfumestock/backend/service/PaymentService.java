package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.PaymentRequest;
import com.perfumestock.backend.dto.PaymentResponse;
import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.entity.BusinessEventType;
import com.perfumestock.backend.entity.Payment;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.exception.BusinessRuleException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.CustomerRepository;
import com.perfumestock.backend.repository.PaymentRepository;
import com.perfumestock.backend.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final CustomerRepository customerRepo;
    private final SaleRepository saleRepo;
    private final BusinessLedgerService ledgerService;

    public PaymentService(PaymentRepository paymentRepo, CustomerRepository customerRepo,
                          SaleRepository saleRepo,
                          BusinessLedgerService ledgerService) {
        this.paymentRepo = paymentRepo;
        this.customerRepo = customerRepo;
        this.saleRepo = saleRepo;
        this.ledgerService = ledgerService;
    }

    public List<PaymentResponse> getByCustomer(Long customerId) {
        return paymentRepo.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream().map(PaymentResponse::from).toList();
    }

    @Transactional
    public PaymentResponse record(PaymentRequest req) {
        Customer c = customerRepo.findById(req.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", req.getCustomerId()));

        if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero");
        }

        Payment p = new Payment();
        p.setCustomer(c);
        p.setAmount(req.getAmount());
        p.setPaymentMethod(req.getPaymentMethod());
        p.setNotes(req.getNotes());

        if (req.getSaleId() != null) {
            Sale sale = saleRepo.findById(req.getSaleId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", req.getSaleId()));

            if (req.getAmount().compareTo(sale.getAmountOwing()) > 0) {
                throw new BusinessRuleException("Payment amount exceeds amount owing (R" + sale.getAmountOwing() + ")");
            }

            sale.setAmountPaid(sale.getAmountPaid().add(req.getAmount()));
            sale.setAmountOwing(sale.getAmountOwing().subtract(req.getAmount()));
            saleRepo.save(sale);
            p.setSale(sale);
        }

        Payment saved = paymentRepo.save(p);
        ledgerService.record(
            BusinessEventType.PAYMENT_RECEIVED,
            "PAYMENT",
            saved.getId(),
            c,
            null,
            saved.getAmount(),
            null,
            "Payment received",
            saved.getPaymentMethod().name()
        );
        return PaymentResponse.from(saved);
    }

    @Transactional
    public PaymentResponse reverse(Long paymentId, String reason) {
        Payment payment = paymentRepo.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getSale() != null) {
            Sale sale = payment.getSale();
            sale.setAmountPaid(sale.getAmountPaid().subtract(payment.getAmount()));
            sale.setAmountOwing(sale.getAmountOwing().add(payment.getAmount()));
            saleRepo.save(sale);
        }

        ledgerService.record(
            BusinessEventType.PAYMENT_REVERSED,
            "PAYMENT",
            payment.getId(),
            payment.getCustomer(),
            null,
            payment.getAmount(),
            null,
            reason != null && !reason.isBlank() ? reason : "Payment reversed",
            payment.getPaymentMethod().name()
        );
        return PaymentResponse.from(payment);
    }
}
