package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.CustomerBalanceResponse;
import com.perfumestock.backend.dto.CustomerLedgerEntry;
import com.perfumestock.backend.dto.CustomerStatementResponse;
import com.perfumestock.backend.entity.BusinessEvent;
import com.perfumestock.backend.entity.BusinessEventType;
import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.entity.Payment;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.BusinessEventRepository;
import com.perfumestock.backend.repository.CustomerRepository;
import com.perfumestock.backend.repository.PaymentRepository;
import com.perfumestock.backend.repository.SaleRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomerLedgerService {

    private static final String BUSINESS_NAME = "ArthurFord Gold Agent Business Manager";
    private static final Sort LEDGER_SORT = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));

    private final CustomerRepository customerRepo;
    private final BusinessEventRepository eventRepo;
    private final SaleRepository saleRepo;
    private final PaymentRepository paymentRepo;

    public CustomerLedgerService(CustomerRepository customerRepo,
                                 BusinessEventRepository eventRepo,
                                 SaleRepository saleRepo,
                                 PaymentRepository paymentRepo) {
        this.customerRepo = customerRepo;
        this.eventRepo = eventRepo;
        this.saleRepo = saleRepo;
        this.paymentRepo = paymentRepo;
    }

    @Transactional(readOnly = true)
    public List<CustomerLedgerEntry> getLedger(Long customerId) {
        return mapEvents(loadEvents(customerId, null, null, null), BigDecimal.ZERO, true);
    }

    @Transactional(readOnly = true)
    public Page<CustomerLedgerEntry> getLedgerPage(Long customerId,
                                                   LocalDateTime startDate,
                                                   LocalDateTime endDate,
                                                   BusinessEventType transactionType,
                                                   Pageable pageable) {
        ensureCustomerExists(customerId);
        Pageable effectivePageable = normalize(pageable);
        Specification<BusinessEvent> spec = ledgerSpec(customerId, startDate, endDate, transactionType);

        Page<BusinessEvent> page = eventRepo.findAll(spec, effectivePageable);
        BigDecimal openingBalance = balanceBeforePage(customerId, startDate, endDate, transactionType, effectivePageable, spec);
        List<CustomerLedgerEntry> entries = mapEvents(page.getContent(), openingBalance, true);
        return new PageImpl<>(entries, effectivePageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public CustomerBalanceResponse getBalance(Long customerId) {
        ensureCustomerExists(customerId);

        CustomerBalanceResponse response = new CustomerBalanceResponse();
        List<BusinessEvent> events = loadEvents(customerId, null, null, null);
        BigDecimal outstanding = calculateBalance(events);

        BigDecimal totalPurchases = saleRepo.findByCustomerIdOrderBySaleDateAsc(customerId).stream()
            .map(Sale::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayments = paymentRepo.findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime lastPaymentDate = paymentRepo.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
            .findFirst()
            .map(Payment::getCreatedAt)
            .orElse(null);

        response.setOutstandingBalance(outstanding);
        response.setTotalPurchases(totalPurchases);
        response.setTotalPayments(totalPayments);
        response.setLastPaymentDate(lastPaymentDate);
        return response;
    }

    @Transactional(readOnly = true)
    public CustomerStatementResponse getStatement(Long customerId) {
        return getStatement(customerId, null, null, null);
    }

    @Transactional(readOnly = true)
    public CustomerStatementResponse getStatement(Long customerId,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate,
                                                  BusinessEventType transactionType) {
        Customer customer = ensureCustomerExists(customerId);
        List<BusinessEvent> events = loadEvents(customerId, startDate, endDate, transactionType);
        BigDecimal openingBalance = openingBalance(customerId, startDate, transactionType);
        List<CustomerLedgerEntry> ledger = mapEvents(events, openingBalance, true);

        BigDecimal totalDebits = ledger.stream().map(CustomerLedgerEntry::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = ledger.stream().map(CustomerLedgerEntry::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal closingBalance = ledger.isEmpty()
            ? openingBalance
            : ledger.get(ledger.size() - 1).getRunningBalance();

        CustomerStatementResponse statement = new CustomerStatementResponse();
        statement.setBusinessName(BUSINESS_NAME);
        statement.setCustomerName(customer.getName());
        statement.setStatementPeriod(buildStatementPeriod(startDate, endDate));
        statement.setStartDate(startDate);
        statement.setEndDate(endDate);
        statement.setOpeningBalance(openingBalance);
        statement.setClosingBalance(closingBalance);
        statement.setTotalDebits(totalDebits);
        statement.setTotalCredits(totalCredits);
        statement.setTransactionCount(ledger.size());
        statement.setGeneratedDate(LocalDateTime.now());
        statement.setTransactions(ledger);
        return statement;
    }

    @Transactional(readOnly = true)
    public BigDecimal getOutstandingBalance(Long customerId) {
        ensureCustomerExists(customerId);
        return calculateBalance(loadEvents(customerId, null, null, null));
    }

    private Customer ensureCustomerExists(Long customerId) {
        return customerRepo.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
    }

    private Pageable normalize(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        if (size <= 0) {
            size = 50;
        }
        return PageRequest.of(page, size, LEDGER_SORT);
    }

    private Specification<BusinessEvent> ledgerSpec(Long customerId,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate,
                                                    BusinessEventType transactionType) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            Join<BusinessEvent, Customer> customerJoin = root.join("customer", JoinType.INNER);
            predicates.add(cb.equal(customerJoin.get("id"), customerId));
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            if (transactionType != null) {
                predicates.add(cb.equal(root.get("eventType"), transactionType));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private List<BusinessEvent> loadEvents(Long customerId,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate,
                                           BusinessEventType transactionType) {
        return eventRepo.findAll(ledgerSpec(customerId, startDate, endDate, transactionType), LEDGER_SORT);
    }

    private BigDecimal balanceBeforePage(Long customerId,
                                         LocalDateTime startDate,
                                         LocalDateTime endDate,
                                         BusinessEventType transactionType,
                                         Pageable page,
                                         Specification<BusinessEvent> spec) {
        if (page.getOffset() <= 0) {
            return openingBalance(customerId, startDate, transactionType);
        }

        int priorSize = Math.toIntExact(Math.min(page.getOffset(), Integer.MAX_VALUE));
        Pageable priorPage = PageRequest.of(0, priorSize, LEDGER_SORT);
        List<BusinessEvent> priorEvents = eventRepo.findAll(spec, priorPage).getContent();
        return calculateBalance(priorEvents);
    }

    private BigDecimal openingBalance(Long customerId,
                                      LocalDateTime startDate,
                                      BusinessEventType transactionType) {
        if (startDate == null) {
            return BigDecimal.ZERO;
        }
        LocalDateTime priorCutoff = startDate.minusNanos(1);
        return calculateBalance(loadEvents(customerId, null, priorCutoff, transactionType));
    }

    private BigDecimal calculateBalance(Collection<BusinessEvent> events) {
        BigDecimal running = BigDecimal.ZERO;
        for (BusinessEvent event : events) {
            LedgerEffect effect = effectFor(event);
            running = running.add(effect.getDebit()).subtract(effect.getCredit());
        }
        return running;
    }

    private List<CustomerLedgerEntry> mapEvents(List<BusinessEvent> events, BigDecimal openingBalance, boolean batchLoadRelated) {
        Map<Long, Sale> sales = new LinkedHashMap<>();
        Map<Long, Payment> payments = new LinkedHashMap<>();
        if (batchLoadRelated) {
            Set<Long> saleIds = events.stream()
                .filter(e -> e.getReferenceId() != null)
                .filter(e -> e.getEventType() == BusinessEventType.SALE_RECORDED
                    || e.getEventType() == BusinessEventType.SALE_REVERSED)
                .map(BusinessEvent::getReferenceId)
                .collect(Collectors.toSet());
            Set<Long> paymentIds = events.stream()
                .filter(e -> e.getReferenceId() != null)
                .filter(e -> e.getEventType() == BusinessEventType.PAYMENT_RECEIVED
                    || e.getEventType() == BusinessEventType.PAYMENT_REVERSED)
                .map(BusinessEvent::getReferenceId)
                .collect(Collectors.toSet());
            if (!saleIds.isEmpty()) {
                Map<Long, Sale> saleMap = saleRepo.findByIdIn(saleIds).stream()
                    .collect(Collectors.toMap(Sale::getId, s -> s, (a, b) -> a, LinkedHashMap::new));
                sales.putAll(saleMap);
            }
            if (!paymentIds.isEmpty()) {
                Map<Long, Payment> paymentMap = paymentRepo.findAllById(paymentIds).stream()
                    .collect(Collectors.toMap(Payment::getId, p -> p, (a, b) -> a, LinkedHashMap::new));
                payments.putAll(paymentMap);
            }
        }
        return mapEvents(events, openingBalance, sales, payments);
    }

    private List<CustomerLedgerEntry> mapEvents(List<BusinessEvent> events,
                                                BigDecimal openingBalance,
                                                Map<Long, Sale> sales,
                                                Map<Long, Payment> payments) {
        List<CustomerLedgerEntry> entries = new ArrayList<>();
        BigDecimal running = openingBalance;

        for (BusinessEvent event : events) {
            CustomerLedgerEntry entry = new CustomerLedgerEntry();
            entry.setDate(event.getCreatedAt());
            entry.setEventType(event.getEventType().name());
            entry.setReferenceId(event.getReferenceId());
            entry.setBusinessEventId(event.getId());
            entry.setDebit(BigDecimal.ZERO);
            entry.setCredit(BigDecimal.ZERO);

            LedgerEffect effect = effectFor(event);
            entry.setDebit(effect.getDebit());
            entry.setCredit(effect.getCredit());
            running = running.add(effect.getDebit()).subtract(effect.getCredit());
            entry.setRunningBalance(running);
            populateDescription(entry, event, sales, payments);
            entries.add(entry);
        }

        return entries;
    }

    private void populateDescription(CustomerLedgerEntry entry,
                                     BusinessEvent event,
                                     Map<Long, Sale> sales,
                                     Map<Long, Payment> payments) {
        switch (event.getEventType()) {
            case SALE_RECORDED, SALE_REVERSED -> {
                Sale sale = sales.get(event.getReferenceId());
                if (sale != null) {
                    entry.setSaleId(sale.getId());
                    entry.setDescription(buildSaleDescription(sale) + (event.getEventType() == BusinessEventType.SALE_REVERSED ? " reversed" : ""));
                    return;
                }
                entry.setDescription(event.getEventType().name().replace('_', ' '));
            }
            case PAYMENT_RECEIVED, PAYMENT_REVERSED -> {
                Payment payment = payments.get(event.getReferenceId());
                if (payment != null) {
                    entry.setPaymentId(payment.getId());
                    entry.setDescription("Payment " + (event.getEventType() == BusinessEventType.PAYMENT_REVERSED ? "reversal" : "received")
                        + " via " + payment.getPaymentMethod().name());
                    return;
                }
                entry.setDescription(event.getEventType().name().replace('_', ' '));
            }
            default -> entry.setDescription(event.getEventType().name().replace('_', ' '));
        }
    }

    private LedgerEffect effectFor(BusinessEvent event) {
        BigDecimal amount = event.getAmount() == null ? BigDecimal.ZERO : event.getAmount();
        return switch (event.getEventType()) {
            case SALE_RECORDED, PAYMENT_REVERSED -> LedgerEffect.debit(amount);
            case PAYMENT_RECEIVED, SALE_REVERSED -> LedgerEffect.credit(amount);
            default -> LedgerEffect.zero();
        };
    }

    private String buildSaleDescription(Sale sale) {
        StringBuilder builder = new StringBuilder();
        builder.append("Sale #").append(sale.getId());
        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            builder.append(" - ");
            builder.append(sale.getItems().get(0).getProduct().getName());
            if (sale.getItems().size() > 1) {
                builder.append(" +").append(sale.getItems().size() - 1).append(" more");
            }
        }
        if (sale.getPaymentType() != null) {
            builder.append(" (").append(sale.getPaymentType().name()).append(")");
        }
        return builder.toString();
    }

    private String buildStatementPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) {
            return "All time";
        }
        String start = startDate != null ? startDate.toString() : "Beginning";
        String end = endDate != null ? endDate.toString() : "Present";
        return start + " to " + end;
    }

    private static final class LedgerEffect {
        private final BigDecimal debit;
        private final BigDecimal credit;

        private LedgerEffect(BigDecimal debit, BigDecimal credit) {
            this.debit = debit;
            this.credit = credit;
        }

        static LedgerEffect debit(BigDecimal amount) {
            return new LedgerEffect(amount, BigDecimal.ZERO);
        }

        static LedgerEffect credit(BigDecimal amount) {
            return new LedgerEffect(BigDecimal.ZERO, amount);
        }

        static LedgerEffect zero() {
            return new LedgerEffect(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal getDebit() { return debit; }
        BigDecimal getCredit() { return credit; }
    }
}
