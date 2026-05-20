package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.SaleRequest;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.repository.ProductRepository;
import com.perfumestock.backend.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final AtomicInteger saleCounter = new AtomicInteger(1);

    @Autowired
    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        initializeCounter();
    }

    private void initializeCounter() {
        long count = saleRepository.count();
        saleCounter.set((int) count + 1);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
    }

    @Transactional
    public Sale recordSale(SaleRequest request, User recordedBy) {
        Product product = productRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        BigDecimal unitPrice = getPriceByTier(product, request.getCustomerTier());
        
        String saleId = generateSaleId();
        
        Sale sale = new Sale(
                saleId,
                product,
                product.getName(),
                product.getCategory(),
                request.getQuantity(),
                unitPrice,
                request.getCustomerTier(),
                recordedBy
        );

        // Reduce product stock
        product.reduceStock(request.getQuantity());
        productRepository.save(product);

        return saleRepository.save(sale);
    }

    private BigDecimal getPriceByTier(Product product, Sale.CustomerTier tier) {
        return switch (tier) {
            case RETAIL -> product.getRetailPrice();
            case REWARDS -> product.getRewardsPrice();
            case GOLD -> product.getGoldPrice();
            case VIP -> product.getVipPrice();
        };
    }

    private String generateSaleId() {
        return String.format("SAL%03d", saleCounter.getAndIncrement());
    }

    public List<Sale> searchSalesByProductName(String productName) {
        return saleRepository.findByProductNameContainingIgnoreCase(productName);
    }

    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByDateRange(startDate, endDate);
    }

    public List<Sale> getTodaySales() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return saleRepository.findSince(startOfDay);
    }

    public Double getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Double revenue = saleRepository.sumTodayRevenue(startOfDay);
        return revenue != null ? revenue : 0.0;
    }

    public Long getTodaySalesCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long count = saleRepository.countTodaySales(startOfDay);
        return count != null ? count : 0L;
    }
}
