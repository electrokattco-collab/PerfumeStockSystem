package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.dto.ProcurementItemRequest;
import com.perfumestock.backend.dto.ProcurementRequest;
import com.perfumestock.backend.entity.Procurement;
import com.perfumestock.backend.entity.ProcurementItem;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.exception.BusinessRuleException;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.ProcurementRepository;
import com.perfumestock.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProcurementService {

    private static final Logger log = LoggerFactory.getLogger(ProcurementService.class);

    private final ProcurementRepository procurementRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private final StockMovementService stockMovementService;

    @Autowired
    public ProcurementService(ProcurementRepository procurementRepository,
                              ProductRepository productRepository,
                              AuditLogService auditLogService,
                              StockMovementService stockMovementService) {
        this.procurementRepository = procurementRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
        this.stockMovementService = stockMovementService;
    }

    public PageResponse<Procurement> getAllProcurements(Pageable pageable) {
        Page<Procurement> page = procurementRepository.findAll(pageable);
        return PageResponse.of(page);
    }

    public Procurement getProcurementById(Long id) {
        return procurementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Procurement", "id", id));
    }

    @Transactional
    public Procurement createProcurement(ProcurementRequest request, String username) {
        if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()) {
            if (procurementRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
                throw new DuplicateResourceException("Procurement", "invoiceNumber", request.getInvoiceNumber());
            }
        }

        Procurement procurement = new Procurement();
        procurement.setSupplierName(request.getSupplierName());
        procurement.setSupplierContact(request.getSupplierContact());
        procurement.setInvoiceNumber(request.getInvoiceNumber());
        procurement.setPurchaseDate(request.getPurchaseDate());
        procurement.setInvoiceFilePath(request.getInvoiceFilePath());
        procurement.setInvoiceType(request.getInvoiceType());
        procurement.setVatAmount(request.getVatAmount() != null ? request.getVatAmount() : BigDecimal.ZERO);
        procurement.setNotes(request.getNotes());
        procurement.setUploadedBy(username);
        procurement.setStatus("DRAFT");

        for (ProcurementItemRequest itemReq : request.getItems()) {
            ProcurementItem item = new ProcurementItem();
            item.setProductName(itemReq.getProductName());
            item.setBrand(itemReq.getBrand());
            item.setCategory(itemReq.getCategory());
            item.setQuantityPurchased(itemReq.getQuantityPurchased());
            item.setBuyPrice(itemReq.getBuyPrice());
            item.setSuggestedSellingPrice(itemReq.getSuggestedSellingPrice());
            item.setExpectedProfit(itemReq.getExpectedProfit());
            item.setBarcode(itemReq.getBarcode());
            item.setExpiryDate(itemReq.getExpiryDate());
            item.setBatchNumber(itemReq.getBatchNumber());
            procurement.addItem(item);
        }

        procurement.recalculateTotals();
        Procurement saved = procurementRepository.save(procurement);

        auditLogService.log("Procurement", saved.getId(), "CREATED",
                "Procurement created for supplier: " + saved.getSupplierName(),
                null, username);

        log.info("Created procurement {} for supplier: {}", saved.getId(), saved.getSupplierName());
        return saved;
    }

    @Transactional
    public Procurement updateProcurement(Long id, ProcurementRequest request, String username) {
        Procurement procurement = getProcurementById(id);

        if ("CONFIRMED".equals(procurement.getStatus())) {
            throw new BusinessRuleException("Cannot edit a confirmed procurement. Cancel it first.");
        }

        if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()
                && !request.getInvoiceNumber().equals(procurement.getInvoiceNumber())) {
            if (procurementRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
                throw new DuplicateResourceException("Procurement", "invoiceNumber", request.getInvoiceNumber());
            }
        }

        procurement.setSupplierName(request.getSupplierName());
        procurement.setSupplierContact(request.getSupplierContact());
        procurement.setInvoiceNumber(request.getInvoiceNumber());
        procurement.setPurchaseDate(request.getPurchaseDate());
        procurement.setInvoiceFilePath(request.getInvoiceFilePath());
        procurement.setInvoiceType(request.getInvoiceType());
        procurement.setVatAmount(request.getVatAmount() != null ? request.getVatAmount() : BigDecimal.ZERO);
        procurement.setNotes(request.getNotes());

        procurement.getItems().clear();
        for (ProcurementItemRequest itemReq : request.getItems()) {
            ProcurementItem item = new ProcurementItem();
            item.setProductName(itemReq.getProductName());
            item.setBrand(itemReq.getBrand());
            item.setCategory(itemReq.getCategory());
            item.setQuantityPurchased(itemReq.getQuantityPurchased());
            item.setBuyPrice(itemReq.getBuyPrice());
            item.setSuggestedSellingPrice(itemReq.getSuggestedSellingPrice());
            item.setExpectedProfit(itemReq.getExpectedProfit());
            item.setBarcode(itemReq.getBarcode());
            item.setExpiryDate(itemReq.getExpiryDate());
            item.setBatchNumber(itemReq.getBatchNumber());
            procurement.addItem(item);
        }

        procurement.recalculateTotals();
        Procurement saved = procurementRepository.save(procurement);

        auditLogService.log("Procurement", saved.getId(), "UPDATED",
                "Procurement updated for supplier: " + saved.getSupplierName(),
                null, username);

        log.info("Updated procurement {} for supplier: {}", saved.getId(), saved.getSupplierName());
        return saved;
    }

    @Transactional
    public void deleteProcurement(Long id, String username) {
        Procurement procurement = getProcurementById(id);

        if ("CONFIRMED".equals(procurement.getStatus())) {
            throw new BusinessRuleException("Cannot delete a confirmed procurement.");
        }

        procurementRepository.delete(procurement);

        auditLogService.log("Procurement", id, "DELETED",
                "Procurement deleted for supplier: " + procurement.getSupplierName(),
                null, username);

        log.info("Deleted procurement {} for supplier: {}", id, procurement.getSupplierName());
    }

    public PageResponse<Procurement> searchProcurements(String supplierName, String invoiceNumber,
                                                         String status, Pageable pageable) {
        Page<Procurement> page = procurementRepository.search(supplierName, invoiceNumber, status, pageable);
        return PageResponse.of(page);
    }

    @Transactional
    public Procurement confirmProcurement(Long id, String username) {
        Procurement procurement = getProcurementById(id);

        if ("CONFIRMED".equals(procurement.getStatus())) {
            throw new BusinessRuleException("Procurement is already confirmed.");
        }

        if (procurement.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot confirm a procurement with no items.");
        }

        for (ProcurementItem item : procurement.getItems()) {
            createOrUpdateProduct(item, username);
        }

        procurement.setStatus("CONFIRMED");
        Procurement saved = procurementRepository.save(procurement);

        auditLogService.log("Procurement", saved.getId(), "CONFIRMED",
                "Procurement confirmed. " + saved.getItems().size() + " products processed.",
                null, username);

        log.info("Confirmed procurement {} - inventory updated", saved.getId());
        return saved;
    }

    private void createOrUpdateProduct(ProcurementItem item, String username) {
        List<Product> matches = productRepository.findByNameContainingIgnoreCase(item.getProductName());
        Optional<Product> existingProduct = matches.stream()
                .filter(p -> p.getName().equalsIgnoreCase(item.getProductName()))
                .findFirst();

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.addStock(item.getQuantityPurchased());
            product.setBuyPrice(item.getBuyPrice());
            if (item.getSuggestedSellingPrice() != null) {
                product.setSellPrice(item.getSuggestedSellingPrice());
            }
            product = productRepository.save(product);

            stockMovementService.record(product.getId(), "PURCHASE", item.getQuantityPurchased(),
                    item.getBuyPrice(), item.getProcurement().getId(), "PROCUREMENT",
                    "Procurement #" + item.getProcurement().getId(), username);

            log.info("Updated product '{}' - added {} units", product.getName(), item.getQuantityPurchased());
        } else {
            String productId = item.getProductName().trim().replace("\\s+", "-").toLowerCase();
            BigDecimal sellPrice = item.getSuggestedSellingPrice() != null
                    ? item.getSuggestedSellingPrice()
                    : item.getBuyPrice().multiply(new BigDecimal("1.5")).setScale(2, RoundingMode.HALF_UP);

            Product product = new Product(productId, item.getProductName(),
                    item.getCategory() != null ? item.getCategory() : "General",
                    null, item.getBuyPrice(), sellPrice, item.getQuantityPurchased());
            if (item.getBarcode() != null) {
                product.setBarcode(item.getBarcode());
            }
            product = productRepository.save(product);

            stockMovementService.record(product.getId(), "PURCHASE", item.getQuantityPurchased(),
                    item.getBuyPrice(), item.getProcurement().getId(), "PROCUREMENT",
                    "Procurement #" + item.getProcurement().getId(), username);

            log.info("Created product '{}' with {} units from procurement", product.getName(), item.getQuantityPurchased());
        }
    }

    @Transactional
    public Procurement importOcrData(ProcurementRequest request, String username) {
        Procurement procurement = new Procurement();
        procurement.setSupplierName(request.getSupplierName());
        procurement.setSupplierContact(request.getSupplierContact());
        procurement.setInvoiceNumber(request.getInvoiceNumber());
        procurement.setPurchaseDate(request.getPurchaseDate());
        procurement.setInvoiceFilePath(request.getInvoiceFilePath());
        procurement.setInvoiceType(request.getInvoiceType());
        procurement.setVatAmount(request.getVatAmount() != null ? request.getVatAmount() : BigDecimal.ZERO);
        procurement.setNotes(request.getNotes());
        procurement.setUploadedBy(username);
        procurement.setStatus("REVIEWING");

        if (request.getItems() != null) {
            for (ProcurementItemRequest itemReq : request.getItems()) {
                ProcurementItem item = new ProcurementItem();
                item.setProductName(itemReq.getProductName());
                item.setBrand(itemReq.getBrand());
                item.setCategory(itemReq.getCategory());
                item.setQuantityPurchased(itemReq.getQuantityPurchased());
                item.setBuyPrice(itemReq.getBuyPrice());
                item.setSuggestedSellingPrice(itemReq.getSuggestedSellingPrice());
                item.setExpectedProfit(itemReq.getExpectedProfit());
                item.setBarcode(itemReq.getBarcode());
                procurement.addItem(item);
            }
        }

        procurement.recalculateTotals();
        Procurement saved = procurementRepository.save(procurement);

        auditLogService.log("Procurement", saved.getId(), "OCR_IMPORTED",
                "OCR data imported for review. Supplier: " + saved.getSupplierName(),
                null, username);

        log.info("OCR data imported into procurement {} for review", saved.getId());
        return saved;
    }

    public Map<String, Object> getDashboardStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);

        long todayPurchases = procurementRepository.countByPurchaseDateRange(todayStart, todayEnd);
        long monthPurchases = procurementRepository.countByPurchaseDateRange(monthStart, monthEnd);
        BigDecimal monthCost = procurementRepository.sumTotalAmountByDateRange(monthStart, monthEnd);
        long supplierCount = procurementRepository.countDistinctSuppliers();
        List<Procurement> recentProcurements = procurementRepository.findTop10ByOrderByCreatedAtDesc();

        return Map.of(
                "todayPurchases", todayPurchases,
                "monthPurchases", monthPurchases,
                "monthCost", monthCost,
                "supplierCount", supplierCount,
                "recentProcurements", recentProcurements
        );
    }
}
