package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.entity.PurchaseReceipt;
import com.perfumestock.backend.entity.PurchaseReceiptItem;
import com.perfumestock.backend.service.OcrService;
import com.perfumestock.backend.service.ReceiptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receipts")
@Tag(name = "Purchase Receipts", description = "OCR receipt scanning and purchase tracking")
public class ReceiptController {
    private final ReceiptService receiptService;
    private final OcrService ocrService;

    @Autowired
    public ReceiptController(ReceiptService receiptService, OcrService ocrService) {
        this.receiptService = receiptService;
        this.ocrService = ocrService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<PurchaseReceipt>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(receiptService.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseReceipt> getById(@PathVariable Long id) { return ResponseEntity.ok(receiptService.getById(id)); }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<PurchaseReceipt>> getPending() { return ResponseEntity.ok(receiptService.getPending()); }

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> scanReceipt(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(ocrService.processReceipt(file));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseReceipt> create(@RequestBody PurchaseReceipt receipt) {
        return ResponseEntity.ok(receiptService.create(receipt));
    }

    @PutMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseReceipt> updateItems(@PathVariable Long id, @RequestBody List<PurchaseReceiptItem> items) {
        return ResponseEntity.ok(receiptService.updateItems(id, items));
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseReceipt> process(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(receiptService.processReceipt(id, auth.getName()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseReceipt> reject(@PathVariable Long id) {
        return ResponseEntity.ok(receiptService.rejectReceipt(id));
    }
}
