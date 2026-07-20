package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.repository.*;
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
public class EnhancedReportService {
    private static final Logger log = LoggerFactory.getLogger(EnhancedReportService.class);

    private final SaleRepository saleRepo;
    private final ProductRepository productRepo;
    private final ExpenseRepository expenseRepo;
    private final CustomerRepository customerRepo;
    private final PurchaseRepository purchaseRepo;
    private final BusinessTransactionRepository financeRepo;
    private final UserRepository userRepo;
    private final AuditLogRepository auditLogRepo;

    @Autowired
    public EnhancedReportService(SaleRepository saleRepo, ProductRepository productRepo,
                                  ExpenseRepository expenseRepo, CustomerRepository customerRepo,
                                  PurchaseRepository purchaseRepo, BusinessTransactionRepository financeRepo,
                                  UserRepository userRepo, AuditLogRepository auditLogRepo) {
        this.saleRepo = saleRepo;
        this.productRepo = productRepo;
        this.expenseRepo = expenseRepo;
        this.customerRepo = customerRepo;
        this.purchaseRepo = purchaseRepo;
        this.financeRepo = financeRepo;
        this.userRepo = userRepo;
        this.auditLogRepo = auditLogRepo;
    }

    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> d = new HashMap<>();
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfYear = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        
        // Sales metrics
        Double monthRevenue = saleRepo.sumSince(startOfMonth);
        Double yearRevenue = saleRepo.sumSince(startOfYear);
        Long monthSalesCount = saleRepo.countSince(startOfMonth);
        d.put("monthRevenue", monthRevenue != null ? monthRevenue : 0.0);
        d.put("yearRevenue", yearRevenue != null ? yearRevenue : 0.0);
        d.put("monthSalesCount", monthSalesCount != null ? monthSalesCount : 0);
        
        // Expense metrics
        Double monthExpenses = expenseRepo.sumSince(startOfMonth);
        d.put("monthExpenses", monthExpenses != null ? monthExpenses : 0.0);
        
        // Profit
        d.put("monthProfit", (monthRevenue != null ? monthRevenue : 0.0) - (monthExpenses != null ? monthExpenses : 0.0));
        
        // Inventory
        List<Product> products = productRepo.findAll();
        BigDecimal inventoryValue = products.stream()
            .map(p -> p.getSellPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        d.put("totalProducts", products.size());
        d.put("inventoryValue", inventoryValue);
        d.put("lowStockCount", productRepo.findLowStockProducts().size());
        
        // Customers
        d.put("totalCustomers", customerRepo.count());
        Long owingCount = customerRepo.count(); // simplified
        d.put("outstandingCustomers", owingCount);
        
        // Users
        d.put("totalUsers", userRepo.count());
        
        // Recent sales
        List<Sale> recentSales = saleRepo.findSince(LocalDateTime.now().minusDays(7));
        d.put("recentSalesCount", recentSales.size());
        d.put("recentSales", recentSales.stream().limit(10).collect(Collectors.toList()));
        
        return d;
    }

    public Map<String, Object> getManagerDashboard() {
        Map<String, Object> d = new HashMap<>();
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Sales
        Double monthRevenue = saleRepo.sumSince(startOfMonth);
        d.put("monthRevenue", monthRevenue != null ? monthRevenue : 0.0);
        d.put("monthSalesCount", nvl(saleRepo.countSince(startOfMonth)));
        
        // Expenses
        Double monthExpenses = expenseRepo.sumSince(startOfMonth);
        d.put("monthExpenses", monthExpenses != null ? monthExpenses : 0.0);
        
        // Inventory value
        List<Product> products = productRepo.findAll();
        BigDecimal stockValue = products.stream()
            .map(p -> p.getSellPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costValue = products.stream()
            .map(p -> p.getBuyPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        d.put("stockValue", stockValue);
        d.put("costValue", costValue);
        d.put("totalProducts", products.size());
        d.put("lowStockCount", productRepo.findLowStockProducts().size());
        
        // Purchases
        Double totalPurchases = purchaseRepo.sumTotalInventoryCost();
        d.put("totalPurchasesCost", totalPurchases != null ? totalPurchases : 0.0);
        
        // Outstanding debts
        List<com.perfumestock.backend.entity.Customer> allCustomers = customerRepo.findAll();
        BigDecimal totalOwing = allCustomers.stream()
            .map(c -> c.getOutstandingBalance() != null ? c.getOutstandingBalance() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long owingCount = allCustomers.stream()
            .filter(c -> c.getOutstandingBalance() != null && c.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
            .count();
        d.put("totalOwing", totalOwing);
        d.put("owingCustomerCount", owingCount);
        
        return d;
    }

    public Map<String, Object> getSalesDashboard() {
        Map<String, Object> d = new HashMap<>();
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Today
        d.put("todaySalesCount", nvl(saleRepo.countTodaySales(startOfDay)));
        Double todayRevenue = saleRepo.sumTodayRevenue(startOfDay);
        d.put("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);
        
        // Week
        d.put("weekSalesCount", nvl(saleRepo.countSince(startOfWeek)));
        Double weekRevenue = saleRepo.sumSince(startOfWeek);
        d.put("weekRevenue", weekRevenue != null ? weekRevenue : 0.0);
        
        // Month
        d.put("monthSalesCount", nvl(saleRepo.countSince(startOfMonth)));
        Double monthRevenue = saleRepo.sumSince(startOfMonth);
        d.put("monthRevenue", monthRevenue != null ? monthRevenue : 0.0);
        
        // Customers served today
        List<Sale> todaySales = saleRepo.findSince(startOfDay);
        Set<String> customersServed = todaySales.stream()
            .filter(s -> s.getCustomerName() != null)
            .map(Sale::getCustomerName)
            .collect(Collectors.toSet());
        d.put("customersServedToday", customersServed.size());
        
        // Outstanding
        List<com.perfumestock.backend.entity.Customer> allCustomers = customerRepo.findAll();
        long owingCount = allCustomers.stream()
            .filter(c -> c.getOutstandingBalance() != null && c.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
            .count();
        BigDecimal totalOwing = allCustomers.stream()
            .map(c -> c.getOutstandingBalance() != null ? c.getOutstandingBalance() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        d.put("outstandingCount", owingCount);
        d.put("outstandingAmount", totalOwing);
        
        // Low stock alerts
        d.put("lowStockCount", productRepo.findLowStockProducts().size());
        
        // Recent sales
        d.put("recentSales", todaySales.stream().limit(5).collect(Collectors.toList()));
        
        return d;
    }

    public List<Map<String, Object>> getSalesTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            List<Sale> daySales = saleRepo.findByDateRange(dayStart, dayEnd);
            double revenue = daySales.stream().mapToDouble(s -> s.getTotalAmount().doubleValue()).sum();
            trend.add(Map.of(
                "date", dayStart.toLocalDate().toString(),
                "day", dayStart.getDayOfWeek().toString().substring(0, 3),
                "revenue", revenue,
                "count", daySales.size()
            ));
        }
        return trend;
    }

    public List<Map<String, Object>> getExpenseBreakdown() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        List<Object[]> breakdown = expenseRepo.sumByCategorySince(startOfMonth);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : breakdown) {
            result.add(Map.of("category", row[0], "amount", row[1]));
        }
        return result;
    }

    public Map<String, Object> getInventoryReport() {
        Map<String, Object> d = new HashMap<>();
        List<Product> products = productRepo.findAll();
        d.put("totalProducts", products.size());
        d.put("lowStock", productRepo.findLowStockProducts().size());
        d.put("outOfStock", productRepo.findOutOfStockProducts().size());
        
        BigDecimal totalSellValue = products.stream()
            .map(p -> p.getSellPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCostValue = products.stream()
            .map(p -> p.getBuyPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        d.put("totalSellValue", totalSellValue);
        d.put("totalCostValue", totalCostValue);
        
        Map<String, Long> byCategory = products.stream()
            .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
        d.put("byCategory", byCategory);
        
        return d;
    }

    public Map<String, Object> getDebtReport() {
        Map<String, Object> d = new HashMap<>();
        List<com.perfumestock.backend.entity.Customer> all = customerRepo.findAll();
        List<Map<String, Object>> debtors = new ArrayList<>();
        BigDecimal totalOwing = BigDecimal.ZERO;
        
        for (com.perfumestock.backend.entity.Customer c : all) {
            if (c.getOutstandingBalance() != null && c.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
                long daysSince = java.time.Duration.between(c.getCreatedAt(), LocalDateTime.now()).toDays();
                debtors.add(Map.of(
                    "id", c.getId(),
                    "name", c.getName(),
                    "phone", c.getPhone() != null ? c.getPhone() : "",
                    "balance", c.getOutstandingBalance(),
                    "daysSinceCreated", daysSince
                ));
                totalOwing = totalOwing.add(c.getOutstandingBalance());
            }
        }
        d.put("totalOwing", totalOwing);
        d.put("debtorCount", debtors.size());
        d.put("debtors", debtors);
        return d;
    }

    public Map<String, Object> getDailyReport() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return buildReport(start, "Today");
    }

    public Map<String, Object> getWeeklyReport() {
        LocalDateTime start = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0);
        return buildReport(start, "This Week");
    }

    public Map<String, Object> getMonthlyReport() {
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return buildReport(start, "This Month");
    }

    public Map<String, Object> getYearlyReport() {
        LocalDateTime start = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        return buildReport(start, "This Year");
    }

    private Map<String, Object> buildReport(LocalDateTime start, String label) {
        Map<String, Object> report = new HashMap<>();
        List<Sale> sales = saleRepo.findSince(start);
        BigDecimal revenue = sales.stream().map(Sale::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Double expenses = expenseRepo.sumSince(start);
        report.put("period", label);
        report.put("salesCount", sales.size());
        report.put("revenue", revenue);
        report.put("expenses", expenses != null ? expenses : 0.0);
        report.put("profit", revenue.subtract(BigDecimal.valueOf(expenses != null ? expenses : 0.0)));
        return report;
    }

    public List<Sale> getSalesCsv(LocalDateTime start, LocalDateTime end) { return saleRepo.findByDateRange(start, end); }
    public List<Product> getLowStockReport() { return productRepo.findLowStockProducts(); }
    public Map<String, Object> getProfitReport() {
        Map<String, Object> report = new HashMap<>();
        List<Sale> allSales = saleRepo.findAll();
        BigDecimal totalRevenue = allSales.stream().map(Sale::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = allSales.stream().filter(s -> s.getCostOfGoodsSold() != null).map(Sale::getCostOfGoodsSold).reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalRevenue", totalRevenue);
        report.put("totalCost", totalCost);
        report.put("totalProfit", totalRevenue.subtract(totalCost));
        report.put("totalSales", allSales.size());
        return report;
    }

    private long nvl(Long v) { return v != null ? v : 0; }
}
