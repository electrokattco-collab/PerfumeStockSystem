package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.PurchaseRequest;
import com.perfumestock.backend.dto.PurchaseResponse;
import com.perfumestock.backend.entity.*;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepo;
    private final PurchaseItemRepository purchaseItemRepo;
    private final ProductRepository productRepo;
    private final ProductBundleRepository bundleRepo;
    private final StockMovementRepository stockMovementRepo;
    private final BusinessLedgerService ledgerService;

    public PurchaseService(PurchaseRepository purchaseRepo, PurchaseItemRepository purchaseItemRepo,
                           ProductRepository productRepo, ProductBundleRepository bundleRepo,
                           StockMovementRepository stockMovementRepo,
                           BusinessLedgerService ledgerService) {
        this.purchaseRepo = purchaseRepo;
        this.purchaseItemRepo = purchaseItemRepo;
        this.productRepo = productRepo;
        this.bundleRepo = bundleRepo;
        this.stockMovementRepo = stockMovementRepo;
        this.ledgerService = ledgerService;
    }

    public Page<PurchaseResponse> list(Pageable pageable) {
        return purchaseRepo.findAllByOrderByPurchaseDateDesc(pageable).map(PurchaseResponse::from);
    }

    public PurchaseResponse getById(Long id) {
        Purchase p = purchaseRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase", "id", id));
        return PurchaseResponse.from(p);
    }

    public Page<PurchaseResponse> listPending(Pageable pageable) {
        return purchaseRepo.findAllByOrderByPurchaseDateDesc(pageable)
            .map(PurchaseResponse::from);
    }

    @Transactional
    public PurchaseResponse record(PurchaseRequest req) {
        Purchase purchase = new Purchase();
        purchase.setPurchaseDate(req.getPurchaseDate() != null ? req.getPurchaseDate() : LocalDateTime.now());
        purchase.setSourceType(req.getSourceType() != null ? req.getSourceType() : PurchaseSourceType.MANUAL);
        purchase.setStatus(PurchaseStatus.PENDING_REVIEW);
        purchase.setNotes(req.getNotes());
        purchase.setReceiptReference(req.getReceiptReference());
        purchase.setOcrText(req.getOcrText());
        purchase.setOcrConfidence(req.getOcrConfidence());

        purchase = purchaseRepo.save(purchase);

        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseRequest.PurchaseItemRequest itemReq : req.getItems()) {
            Product product = productRepo.findById(itemReq.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            PurchaseItem item = new PurchaseItem(product, itemReq.getQuantity(), itemReq.getUnitCost(), false);
            item.setPurchase(purchase);
            purchase.getItems().add(item);
            purchaseItemRepo.save(item);

            total = total.add(item.getLineTotal());
        }

        purchase.setTotalAmount(total);
        purchase = purchaseRepo.save(purchase);

        ledgerService.record(
            BusinessEventType.PURCHASE_RECORDED,
            "PURCHASE",
            purchase.getId(),
            null,
            null,
            total,
            purchase.getItems().stream().mapToInt(PurchaseItem::getQuantity).sum(),
            "Purchase recorded and awaiting confirmation",
            purchase.getNotes()
        );

        return PurchaseResponse.from(purchase);
    }

    @Transactional
    public PurchaseResponse confirm(Long purchaseId) {
        Purchase purchase = purchaseRepo.findById(purchaseId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase", "id", purchaseId));

        if (purchase.getStatus() == PurchaseStatus.CONFIRMED) {
            return PurchaseResponse.from(purchase);
        }

        BusinessEvent event = ledgerService.record(
            BusinessEventType.PURCHASE_CONFIRMED,
            "PURCHASE",
            purchase.getId(),
            null,
            null,
            purchase.getTotalAmount(),
            purchase.getItems().stream().mapToInt(PurchaseItem::getQuantity).sum(),
            "Purchase confirmed and stock released",
            purchase.getReceiptReference()
        );

        for (PurchaseItem item : purchase.getItems()) {
            Product product = item.getProduct();
            if (product.isCombo()) {
                expandCombo(product, item.getQuantity(), purchase, event);
                continue;
            }

            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepo.save(product);

            StockMovement sm = new StockMovement(
                product,
                "PURCHASE_CONFIRM",
                item.getQuantity(),
                event.getId(),
                "BUSINESS_EVENT",
                "Confirmed purchase #" + purchase.getId()
            );
            sm.setEventId(event.getId());
            stockMovementRepo.save(sm);
        }

        purchase.setStatus(PurchaseStatus.CONFIRMED);
        purchase.setConfirmedAt(LocalDateTime.now());
        purchase.setConfirmedBy(resolveUsername());
        purchaseRepo.save(purchase);
        return PurchaseResponse.from(purchase);
    }

    @Transactional
    public PurchaseResponse reverse(Long purchaseId, String reason) {
        Purchase purchase = purchaseRepo.findById(purchaseId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase", "id", purchaseId));

        BusinessEvent event = ledgerService.record(
            BusinessEventType.PURCHASE_REVERSED,
            "PURCHASE",
            purchase.getId(),
            null,
            null,
            purchase.getTotalAmount(),
            purchase.getItems().stream().mapToInt(PurchaseItem::getQuantity).sum(),
            reason != null && !reason.isBlank() ? reason : "Purchase reversed",
            purchase.getReceiptReference()
        );

        if (purchase.getStatus() == PurchaseStatus.CONFIRMED) {
            for (PurchaseItem item : purchase.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepo.save(product);

                StockMovement sm = new StockMovement(
                    product,
                    "PURCHASE_REVERSAL",
                    -item.getQuantity(),
                    purchase.getId(),
                    "PURCHASE",
                    reason != null ? reason : "Purchase reversal"
                );
                sm.setEventId(event.getId());
                stockMovementRepo.save(sm);
            }
        }

        purchase.setStatus(PurchaseStatus.ARCHIVED);
        purchase.setConfirmedAt(LocalDateTime.now());
        purchase.setConfirmedBy(resolveUsername());
        purchaseRepo.save(purchase);
        return PurchaseResponse.from(purchase);
    }

    private void expandCombo(Product comboProduct, int comboQty, Purchase purchase, BusinessEvent event) {
        List<ProductBundle> bundles = bundleRepo.findByComboProductId(comboProduct.getId());
        for (ProductBundle bundle : bundles) {
            Product component = bundle.getComponentProduct();
            int componentQty = comboQty * bundle.getQuantity();

            component.setStockQuantity(component.getStockQuantity() + componentQty);
            productRepo.save(component);

            StockMovement sm = new StockMovement(
                component,
                "PURCHASE_CONFIRM",
                componentQty,
                event.getId(),
                "BUSINESS_EVENT",
                "Confirmed combo purchase #" + purchase.getId() + " via " + comboProduct.getProductCode()
            );
            sm.setEventId(event.getId());
            stockMovementRepo.save(sm);
        }
    }

    private String resolveUsername() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return "system";
        }
        return auth.getName();
    }
}
