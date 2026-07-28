package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.SaleRequest;
import com.perfumestock.backend.dto.SaleResponse;
import com.perfumestock.backend.entity.*;
import com.perfumestock.backend.exception.InsufficientStockException;
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
public class SaleService {

    private final SaleRepository saleRepo;
    private final SaleItemRepository saleItemRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;
    private final StockMovementRepository stockMovementRepo;
    private final BusinessLedgerService ledgerService;

    public SaleService(SaleRepository saleRepo, SaleItemRepository saleItemRepo,
                       ProductRepository productRepo, CustomerRepository customerRepo,
                       StockMovementRepository stockMovementRepo,
                       BusinessLedgerService ledgerService) {
        this.saleRepo = saleRepo;
        this.saleItemRepo = saleItemRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.stockMovementRepo = stockMovementRepo;
        this.ledgerService = ledgerService;
    }

    public Page<SaleResponse> list(Pageable pageable) {
        return saleRepo.findAllByOrderBySaleDateDesc(pageable).map(SaleResponse::from);
    }

    public Page<SaleResponse> listByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return saleRepo.findByDateRange(start, end, pageable).map(SaleResponse::from);
    }

    public SaleResponse getById(Long id) {
        Sale s = saleRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
        return SaleResponse.from(s);
    }

    public List<SaleResponse> recent() {
        return saleRepo.findTop10ByOrderBySaleDateDesc().stream().map(SaleResponse::from).toList();
    }

    @Transactional
    public SaleResponse record(SaleRequest req) {
        Sale sale = new Sale();
        sale.setSaleDate(req.getSaleDate() != null ? req.getSaleDate() : LocalDateTime.now());
        sale.setPaymentType(req.getPaymentType());

        // Set customer if provided
        if (req.getCustomerId() != null) {
            Customer c = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", req.getCustomerId()));
            sale.setCustomer(c);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (SaleRequest.SaleItemRequest itemReq : req.getItems()) {
            Product product = productRepo.findById(itemReq.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new InsufficientStockException(product.getName(), itemReq.getQuantity(), product.getStockQuantity());
            }

            SaleItem si = new SaleItem(product, itemReq.getQuantity(), itemReq.getUnitPrice(), product.getBuyPrice());
            si.setSale(sale);
            sale.getItems().add(si);

            // Update stock
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepo.save(product);

            // Stock movement
            StockMovement sm = new StockMovement(product, "SALE", -itemReq.getQuantity(),
                null, "SALE", null);
            stockMovementRepo.save(sm);

            totalAmount = totalAmount.add(si.getLineTotal());
            totalCost = totalCost.add(product.getBuyPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        sale.setTotalAmount(totalAmount);
        sale.setCostOfGoodsSold(totalCost);

        // Handle payment
        BigDecimal amountPaid = req.getAmountPaid() != null ? req.getAmountPaid() : BigDecimal.ZERO;
        sale.setAmountPaid(amountPaid);
        sale.setAmountOwing(totalAmount.subtract(amountPaid));

        sale = saleRepo.save(sale);

        ledgerService.record(
            BusinessEventType.SALE_RECORDED,
            "SALE",
            sale.getId(),
            sale.getCustomer(),
            null,
            sale.getAmountOwing(),
            sale.getItems().stream().mapToInt(SaleItem::getQuantity).sum(),
            "Sale recorded",
            sale.getPaymentType().name()
        );

        return SaleResponse.from(sale);
    }

    @Transactional
    public SaleResponse reverse(Long saleId, String reason) {
        Sale sale = saleRepo.findById(saleId)
            .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));

        BusinessEvent event = ledgerService.record(
            BusinessEventType.SALE_REVERSED,
            "SALE",
            sale.getId(),
            sale.getCustomer(),
            null,
            sale.getAmountOwing(),
            sale.getItems().stream().mapToInt(SaleItem::getQuantity).sum(),
            reason != null && !reason.isBlank() ? reason : "Sale reversed",
            sale.getPaymentType() != null ? sale.getPaymentType().name() : null
        );

        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepo.save(product);

            StockMovement sm = new StockMovement(product, "SALE_REVERSAL", item.getQuantity(),
                sale.getId(), "SALE", reason != null ? reason : "Sale reversal");
            sm.setEventId(event.getId());
            stockMovementRepo.save(sm);
        }

        return SaleResponse.from(sale);
    }
}
