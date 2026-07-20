package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.repository.ProductRepository;
import com.perfumestock.backend.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public ReportService(ProductRepository productRepository, SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
    }

    public Map<String, Object> getDashboardSummary() {
        log.debug("Generating dashboard summary");
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", productRepository.count());

        List<Product> lowStock = productRepository.findLowStockProducts();
        summary.put("lowStockCount", lowStock.size());

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        Long todaySalesCount = saleRepository.countTodaySales(startOfDay);
        summary.put("todaySalesCount", todaySalesCount != null ? todaySalesCount : 0);

        Double todayRevenue = saleRepository.sumTodayRevenue(startOfDay);
        summary.put("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);

        Double weekRevenue = saleRepository.sumSince(startOfWeek);
        summary.put("weekRevenue", weekRevenue != null ? weekRevenue : 0.0);

        Long weekSalesCount = saleRepository.countSince(startOfWeek);
        summary.put("weekSalesCount", weekSalesCount != null ? weekSalesCount : 0);

        Double monthRevenue = saleRepository.sumSince(startOfMonth);
        summary.put("monthRevenue", monthRevenue != null ? monthRevenue : 0.0);

        Long monthSalesCount = saleRepository.countSince(startOfMonth);
        summary.put("monthSalesCount", monthSalesCount != null ? monthSalesCount : 0);

        // Stock value - use efficient query
        List<Product> products = productRepository.findAll();
        BigDecimal totalStockValue = products.stream()
                .map(p -> p.getSellPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalStockValue", totalStockValue);

        // Best selling - use recent sales only (last 30 days) instead of all time
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Sale> recentSales = saleRepository.findSince(thirtyDaysAgo);
        String bestSelling = recentSales.stream()
                .filter(s -> s.getProduct() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getProduct().getName(),
                        Collectors.summingInt(Sale::getQuantity)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        summary.put("bestSellingProduct", bestSelling);

        log.debug("Dashboard summary generated: {} products, {} low stock",
                summary.get("totalProducts"), summary.get("lowStockCount"));
        return summary;
    }

    public Map<String, Object> getProfitReport() {
        log.debug("Generating profit report");
        Map<String, Object> report = new HashMap<>();
        List<Sale> allSales = saleRepository.findAll();

        BigDecimal totalRevenue = allSales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = allSales.stream()
                .filter(s -> s.getCostOfGoodsSold() != null)
                .map(Sale::getCostOfGoodsSold)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.put("totalRevenue", totalRevenue);
        report.put("totalCost", totalCost);
        report.put("totalProfit", totalRevenue.subtract(totalCost));
        report.put("totalSales", allSales.size());
        return report;
    }

    public Map<String, Object> getDailyReport() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return buildReport(start, "Today");
    }

    public Map<String, Object> getWeeklyReport() {
        LocalDateTime start = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0);
        return buildReport(start, "This Week");
    }

    public Map<String, Object> getMonthlyReport() {
        LocalDateTime start = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return buildReport(start, "This Month");
    }

    private Map<String, Object> buildReport(LocalDateTime start, String label) {
        Map<String, Object> report = new HashMap<>();
        List<Sale> sales = saleRepository.findSince(start);

        BigDecimal revenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Long count = saleRepository.countSince(start);

        report.put("period", label);
        report.put("salesCount", count != null ? count : 0);
        report.put("revenue", revenue);
        report.put("sales", sales);
        return report;
    }

    public List<Sale> getSalesCsv(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByDateRange(start, end);
    }

    public List<Product> getLowStockReport() {
        return productRepository.findLowStockProducts();
    }
}
