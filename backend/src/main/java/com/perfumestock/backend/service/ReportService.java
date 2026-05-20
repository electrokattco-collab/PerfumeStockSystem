package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.repository.ProductRepository;
import com.perfumestock.backend.repository.PurchaseRepository;
import com.perfumestock.backend.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;

    @Autowired
    public ReportService(ProductRepository productRepository, 
                         SaleRepository saleRepository,
                         PurchaseRepository purchaseRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Total products
        long totalProducts = productRepository.count();
        summary.put("totalProducts", totalProducts);
        
        // Low stock count
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        summary.put("lowStockCount", lowStockProducts.size());
        
        // Today's sales
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long todaySalesCount = saleRepository.countTodaySales(startOfDay);
        summary.put("todaySalesCount", todaySalesCount != null ? todaySalesCount : 0);
        
        // Today's revenue
        Double todayRevenue = saleRepository.sumTodayRevenue(startOfDay);
        summary.put("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);
        
        // Total stock value (retail)
        List<Product> products = productRepository.findAll();
        BigDecimal totalStockValue = products.stream()
                .map(p -> p.getRetailPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalStockValue", totalStockValue);
        
        return summary;
    }

    public Map<String, Object> getProfitReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<Sale> allSales = saleRepository.findAll();
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (Sale sale : allSales) {
            totalRevenue = totalRevenue.add(sale.getTotalAmount());
            if (sale.getCostOfGoodsSold() != null) {
                totalCost = totalCost.add(sale.getCostOfGoodsSold());
            }
        }
        
        BigDecimal totalProfit = totalRevenue.subtract(totalCost);
        
        report.put("totalRevenue", totalRevenue);
        report.put("totalCost", totalCost);
        report.put("totalProfit", totalProfit);
        report.put("totalSales", allSales.size());
        
        // Profit by tier
        Map<String, BigDecimal> profitByTier = new HashMap<>();
        for (Sale.CustomerTier tier : Sale.CustomerTier.values()) {
            BigDecimal tierProfit = allSales.stream()
                    .filter(s -> s.getCustomerTier() == tier)
                    .map(Sale::getProfit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            profitByTier.put(tier.name(), tierProfit);
        }
        report.put("profitByTier", profitByTier);
        
        return report;
    }

    public List<Product> getLowStockReport() {
        return productRepository.findLowStockProducts();
    }

    public Double getInventoryCost() {
        Double cost = purchaseRepository.sumTotalInventoryCost();
        return cost != null ? cost : 0.0;
    }
}
