package com.perfumestock.backend.service;

import com.perfumestock.backend.service.AuditLogService;
import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public PageResponse<Customer> getAllCustomers(Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(pageable);
        return PageResponse.of(page);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional
    public Customer findOrCreate(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        Customer customer = customerRepository.findByNameIgnoreCase(name.trim());
        if (customer == null) {
            customer = new Customer(name.trim(), phone);
            customerRepository.save(customer);
            log.info("Created new customer: {}", name.trim());
        } else if (phone != null && !phone.trim().isEmpty()) {
            customer.setPhone(phone.trim());
            customerRepository.save(customer);
        }
        return customer;
    }

    @Transactional
    public Customer createCustomer(Customer c) {
        log.info("Creating customer: {}", c.getName());
        return customerRepository.save(c);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer updated) {
        Customer c = getCustomerById(id);
        c.setName(updated.getName());
        c.setPhone(updated.getPhone());
        log.info("Updated customer: {} (id: {})", c.getName(), id);
        return customerRepository.save(c);
    }

    @Transactional
    public void addOwing(Long customerId, BigDecimal amount) {
        Customer c = getCustomerById(customerId);
        c.setOutstandingBalance(c.getOutstandingBalance().add(amount));
        customerRepository.save(c);
        log.debug("Added owing to customer {}: {} (new balance: {})",
                c.getName(), amount, c.getOutstandingBalance());
    }

    @Transactional
    public void reduceOwing(Long customerId, BigDecimal amount) {
        Customer c = getCustomerById(customerId);
        c.setOutstandingBalance(c.getOutstandingBalance().subtract(amount));
        if (c.getOutstandingBalance().compareTo(BigDecimal.ZERO) < 0) {
            c.setOutstandingBalance(BigDecimal.ZERO);
        }
        customerRepository.save(c);
        log.debug("Reduced owing from customer {}: {} (new balance: {})",
                c.getName(), amount, c.getOutstandingBalance());
    }

    public List<Customer> searchByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    public PageResponse<Customer> searchByName(String name, Pageable pageable) {
        Page<Customer> page = customerRepository.findByNameContainingIgnoreCase(name, pageable);
        return PageResponse.of(page);
    }
}
