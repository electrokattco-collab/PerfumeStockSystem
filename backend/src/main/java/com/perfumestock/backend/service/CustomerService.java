package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.CustomerRequest;
import com.perfumestock.backend.dto.CustomerResponse;
import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final CustomerLedgerService ledgerService;

    public CustomerService(CustomerRepository customerRepo, CustomerLedgerService ledgerService) {
        this.customerRepo = customerRepo;
        this.ledgerService = ledgerService;
    }

    public Page<CustomerResponse> list(Pageable pageable) {
        return customerRepo.findAllByOrderByNameAsc(pageable)
            .map(c -> CustomerResponse.from(c, ledgerService.getOutstandingBalance(c.getId())));
    }

    public Page<CustomerResponse> search(String q, Pageable pageable) {
        return customerRepo.search(q, pageable)
            .map(c -> CustomerResponse.from(c, ledgerService.getOutstandingBalance(c.getId())));
    }

    public CustomerResponse getById(Long id) {
        Customer c = customerRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return CustomerResponse.from(c, ledgerService.getOutstandingBalance(c.getId()));
    }

    public List<CustomerResponse> getDebtors() {
        return customerRepo.findAll().stream()
            .map(c -> CustomerResponse.from(c, ledgerService.getOutstandingBalance(c.getId())))
            .filter(c -> c.getOutstandingBalance().compareTo(java.math.BigDecimal.ZERO) > 0)
            .sorted((a, b) -> b.getOutstandingBalance().compareTo(a.getOutstandingBalance()))
            .toList();
    }

    @Transactional
    public CustomerResponse create(CustomerRequest req) {
        Customer c = new Customer();
        c.setName(req.getName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        c.setNotes(req.getNotes());
        Customer saved = customerRepo.save(c);
        return CustomerResponse.from(saved, ledgerService.getOutstandingBalance(saved.getId()));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest req) {
        Customer c = customerRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        c.setName(req.getName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        c.setNotes(req.getNotes());
        Customer saved = customerRepo.save(c);
        return CustomerResponse.from(saved, ledgerService.getOutstandingBalance(saved.getId()));
    }
}
