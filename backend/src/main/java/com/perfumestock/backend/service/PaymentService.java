package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.entity.PaymentHistory;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.exception.BusinessRuleException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.CustomerRepository;
import com.perfumestock.backend.repository.PaymentHistoryRepository;
import com.perfumestock.backend.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentHistoryRepository paymentRepo;
    private final CustomerRepository customerRepo;
    private final SaleRepository saleRepo;

    @Autowired
    public PaymentService(PaymentHistoryRepository paymentRepo, CustomerRepository customerRepo, SaleRepository saleRepo) {
        this.paymentRepo = paymentRepo;
        this.customerRepo = customerRepo;
        this.saleRepo = saleRepo;
    }

    public List<PaymentHistory> getByCustomer(Long customerId) { return paymentRepo.findByCustomerIdOrderByCreatedAtDesc(customerId); }

    @Transactional
    public PaymentHistory recordPayment(Long customerId, Long saleId, BigDecimal amount, String paymentMethod, String notes, String user) {
        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        
        Sale sale = null;
        if (saleId != null) {
            sale = saleRepo.findById(saleId).orElse(null);
        }
        
        BigDecimal previousOwing = customer.getOutstandingBalance();
        if (amount.compareTo(previousOwing) > 0) {
            throw new BusinessRuleException("Payment amount R" + amount + " exceeds outstanding balance R" + previousOwing);
        }
        
        // Update customer balance
        customer.setOutstandingBalance(previousOwing.subtract(amount));
        customerRepo.save(customer);
        
        // Update sale if linked
        if (sale != null && sale.getAmountOwing() != null) {
            BigDecimal saleOwing = sale.getAmountOwing();
            if (amount.compareTo(saleOwing) >= 0) {
                sale.setAmountOwing(BigDecimal.ZERO);
                sale.setPaid(true);
            } else {
                sale.setAmountOwing(saleOwing.subtract(amount));
            }
            saleRepo.save(sale);
        }
        
        String paymentType = amount.compareTo(previousOwing) >= 0 ? "FULL" : "PARTIAL";
        
        PaymentHistory payment = new PaymentHistory();
        payment.setCustomer(customer);
        payment.setSale(sale);
        payment.setAmount(amount);
        payment.setPaymentType(paymentType);
        payment.setPaymentMethod(paymentMethod);
        payment.setNotes(notes);
        payment.setCreatedBy(user);
        
        log.info("Payment: R{} from {} ({})", amount, customer.getName(), paymentType);
        return paymentRepo.save(payment);
    }
}
