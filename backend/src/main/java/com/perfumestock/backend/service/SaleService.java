package com.perfumestock.backend.service;

import com.perfumestock.backend.service.AuditLogService;
import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.dto.SaleItemRequest;
import com.perfumestock.backend.dto.SaleRequest;
import com.perfumestock.backend.exception.BusinessRuleException;
import com.perfumestock.backend.exception.InsufficientStockException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.entity.SaleItem;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.repository.ProductRepository;
import com.perfumestock.backend.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    private static final Logger log = LoggerFactory.getLogger(SaleService.class);

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;

    @Autowired
    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       CustomerService customerService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.customerService = customerService;
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public PageResponse<Sale> getAllSales(Pageable pageable) {
        Page<Sale> page = saleRepository.findAll(pageable);
        return PageResponse.of(page);
    }

    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
    }

    public PageResponse<Sale> searchSales(String name, String customer, Pageable pageable) {
        Page<Sale> page = saleRepository.search(name, customer, pageable);
        return PageResponse.of(page);
    }

    @Transactional
    public Sale recordSale(SaleRequest request, User recordedBy) {
        boolean hasItems = request.getItems() != null && !request.getItems().isEmpty();
        boolean hasSingle = request.getProductName() != null && !request.getProductName().isEmpty();
        if (!hasItems && !hasSingle) {
            throw new BusinessRuleException("Either product name or items list is required");
        }

        String saleId = generateSaleId();
        Sale sale = new Sale();
        sale.setSaleId(saleId);
        sale.setRecordedBy(recordedBy);
        sale.setCustomerName(request.getCustomerName());

        Customer customer = customerService.findOrCreate(
                request.getCustomerName(), request.getCustomerPhone());
        if (customer != null) {
            sale.setCustomer(customer);
        }

        if (hasItems) {
            processMultiItemSale(sale, request.getItems());
        } else {
            processSingleItemSale(sale, request);
        }

        sale.setPaid(request.getPaid() != null ? request.getPaid() : false);
        BigDecimal total = sale.getTotalAmount();
        sale.setAmountOwing(sale.getPaid() ? BigDecimal.ZERO : total);

        if (customer != null && !sale.getPaid()) {
            customerService.addOwing(customer.getId(), total);
        }

        log.info("Sale recorded: {} (total: {})", saleId, total);
        return saleRepository.save(sale);
    }

    private void processMultiItemSale(Sale sale, List<SaleItemRequest> items) {
        int totalQty = 0;
        for (SaleItemRequest itemReq : items) {
            int qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
            SaleItem item = new SaleItem(itemReq.getProductName(), qty, itemReq.getUnitPrice());

            if (itemReq.getProductId() != null && !itemReq.getProductId().isEmpty()) {
                Product product = productRepository.findByProductId(itemReq.getProductId()).orElse(null);
                if (product != null) {
                    if (product.getStockQuantity() < qty) {
                        throw new InsufficientStockException(product.getName(), qty, product.getStockQuantity());
                    }
                    product.reduceStock(qty);
                    productRepository.save(product);
                }
            }
            sale.addItem(item);
            totalQty += qty;
        }
        sale.setProductName("Multiple items (" + sale.getItems().size() + " product types)");
        sale.setQuantity(totalQty);
        sale.setUnitPrice(BigDecimal.ZERO);
    }

    private void processSingleItemSale(Sale sale, SaleRequest request) {
        int qty = request.getQuantity() != null ? request.getQuantity() : 1;
        sale.setProductName(request.getProductName());
        sale.setQuantity(qty);
        sale.setUnitPrice(request.getUnitPrice());

        if (request.getProductId() != null && !request.getProductId().isEmpty()) {
            Product product = productRepository.findByProductId(request.getProductId()).orElse(null);
            if (product != null) {
                sale.setProduct(product);
                sale.setCategory(product.getCategory());
                if (product.getStockQuantity() < qty) {
                    throw new InsufficientStockException(product.getName(), qty, product.getStockQuantity());
                }
                product.reduceStock(qty);
                productRepository.save(product);
            }
        }
    }

    @Transactional
    public Sale markAsPaid(Long saleId) {
        Sale sale = getSaleById(saleId);
        sale.setPaid(true);
        BigDecimal previousOwing = sale.getAmountOwing();
        sale.setAmountOwing(BigDecimal.ZERO);

        if (sale.getCustomer() != null && previousOwing != null
                && previousOwing.compareTo(BigDecimal.ZERO) > 0) {
            customerService.reduceOwing(sale.getCustomer().getId(), previousOwing);
        }

        log.info("Sale marked as paid: {}", sale.getSaleId());
        return saleRepository.save(sale);
    }

    @Transactional
    public void deleteSale(Long id) {
        Sale sale = getSaleById(id);
        if (sale.getProduct() != null) {
            sale.getProduct().addStock(sale.getQuantity());
            productRepository.save(sale.getProduct());
        }
        log.info("Sale deleted: {}", sale.getSaleId());
        saleRepository.delete(sale);
    }

    @Transactional
    public Sale updateSale(Long id, SaleRequest request) {
        Sale sale = getSaleById(id);

        if (request.getProductName() != null) {
            sale.setProductName(request.getProductName());
        }
        if (request.getQuantity() != null) {
            sale.setQuantity(request.getQuantity());
        }
        if (request.getUnitPrice() != null) {
            sale.setUnitPrice(request.getUnitPrice());
        }
        if (request.getCustomerName() != null) {
            sale.setCustomerName(request.getCustomerName());
        }
        if (request.getPaid() != null) {
            boolean wasPaid = sale.getPaid();
            sale.setPaid(request.getPaid());
            if (!wasPaid && request.getPaid()) {
                BigDecimal previousOwing = sale.getAmountOwing();
                sale.setAmountOwing(BigDecimal.ZERO);
                if (sale.getCustomer() != null && previousOwing != null
                        && previousOwing.compareTo(BigDecimal.ZERO) > 0) {
                    customerService.reduceOwing(sale.getCustomer().getId(), previousOwing);
                }
            } else if (wasPaid && !request.getPaid()) {
                sale.setAmountOwing(sale.getTotalAmount());
                if (sale.getCustomer() != null) {
                    customerService.addOwing(sale.getCustomer().getId(), sale.getTotalAmount());
                }
            }
        }

        return saleRepository.save(sale);
    }

    private String generateSaleId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "SAL-" + timestamp + "-" + random;
    }

    public List<Sale> searchSalesByProductName(String name) {
        return saleRepository.findByProductNameContainingIgnoreCase(name);
    }

    public List<Sale> getSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByDateRange(start, end);
    }

    public PageResponse<Sale> getSalesByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        Page<Sale> page = saleRepository.findByDateRange(start, end, pageable);
        return PageResponse.of(page);
    }

    public List<Sale> getTodaySales() {
        return saleRepository.findSince(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
    }
}
