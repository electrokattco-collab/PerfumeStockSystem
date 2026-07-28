package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.*;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.repository.BusinessEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BusinessLedgerService {

    private final BusinessEventRepository eventRepo;

    public BusinessLedgerService(BusinessEventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Transactional
    public BusinessEvent record(BusinessEventType type, String referenceType, Long referenceId,
                                Customer customer, Product product, BigDecimal amount, Integer quantity,
                                String notes, String payload) {
        if (eventRepo.existsByEventTypeAndReferenceTypeAndReferenceId(type, referenceType, referenceId)) {
            throw new DuplicateResourceException("BusinessEvent", "reference", type + ":" + referenceType + ":" + referenceId);
        }

        BusinessEvent event = new BusinessEvent();
        event.setEventType(type);
        event.setReferenceType(referenceType);
        event.setReferenceId(referenceId);
        event.setCustomer(customer);
        event.setProduct(product);
        event.setAmount(amount != null ? amount : BigDecimal.ZERO);
        event.setQuantity(quantity != null ? quantity : 0);
        event.setNotes(notes);
        event.setPayload(payload);
        return eventRepo.save(event);
    }
}
