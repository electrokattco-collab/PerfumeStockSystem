package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.entity.PurchaseReceipt;
import com.perfumestock.backend.entity.PurchaseReceiptItem;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.PurchaseReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReceiptService {
    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    private final PurchaseReceiptRepository repository;

    @Autowired
    public ReceiptService(PurchaseReceiptRepository repository) { this.repository = repository; }

    public PageResponse<PurchaseReceipt> getAll(Pageable pageable) { return PageResponse.of(repository.findAll(pageable)); }
    public PurchaseReceipt getById(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", id)); }
    public List<PurchaseReceipt> getPending() { return repository.findByStatus("PENDING"); }

    @Transactional
    public PurchaseReceipt create(PurchaseReceipt r) {
        r.setStatus("PENDING");
        log.info("Created receipt: supplier={}, total={}", r.getSupplierName(), r.getTotalAmount());
        return repository.save(r);
    }

    @Transactional
    public PurchaseReceipt processReceipt(Long id, String processedBy) {
        PurchaseReceipt receipt = getById(id);
        receipt.setStatus("PROCESSED");
        receipt.setProcessedBy(processedBy);
        log.info("Processed receipt: {} by {}", receipt.getReceiptNumber(), processedBy);
        return repository.save(receipt);
    }

    @Transactional
    public PurchaseReceipt rejectReceipt(Long id) {
        PurchaseReceipt receipt = getById(id);
        receipt.setStatus("REJECTED");
        return repository.save(receipt);
    }

    @Transactional
    public PurchaseReceipt updateItems(Long id, List<PurchaseReceiptItem> items) {
        PurchaseReceipt receipt = getById(id);
        receipt.getItems().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseReceiptItem item : items) {
            item.setReceipt(receipt);
            if (item.getTotalCost() == null && item.getUnitCost() != null) {
                item.setTotalCost(item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            total = total.add(item.getTotalCost() != null ? item.getTotalCost() : BigDecimal.ZERO);
            receipt.getItems().add(item);
        }
        receipt.setTotalAmount(total);
        return repository.save(receipt);
    }
}
