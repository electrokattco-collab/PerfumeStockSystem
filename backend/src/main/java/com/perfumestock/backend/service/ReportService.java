package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.*;
import com.perfumestock.backend.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportService {

    private final SaleRepository saleRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;
    private final PurchaseRepository purchaseRepo;
    private final StockMovementRepository stockMovementRepo;
    private final BusinessEventRepository eventRepo;
    private final CustomerLedgerService customerLedgerService;
    private final PaymentRepository paymentRepo;

    public ReportService(SaleRepository saleRepo, ProductRepository productRepo,
                         CustomerRepository customerRepo, PurchaseRepository purchaseRepo,
                         StockMovementRepository stockMovementRepo,
                         BusinessEventRepository eventRepo,
                         CustomerLedgerService customerLedgerService,
                         PaymentRepository paymentRepo) {
        this.saleRepo = saleRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.purchaseRepo = purchaseRepo;
        this.stockMovementRepo = stockMovementRepo;
        this.eventRepo = eventRepo;
        this.customerLedgerService = customerLedgerService;
        this.paymentRepo = paymentRepo;
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> d = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> customerBalances = loadCustomerBalances();
        BigDecimal todayRevenue = saleRepo.sumTotalSince(startOfDay);
        BigDecimal todayCost = saleRepo.sumCostSince(startOfDay);
        BigDecimal monthRevenue = saleRepo.sumTotalSince(startOfMonth);
        BigDecimal monthCost = saleRepo.sumCostSince(startOfMonth);
        BigDecimal cashReceivedMonth = saleRepo.sumPaidSince(startOfMonth);
        BigDecimal cashReceivedToday = saleRepo.sumPaidSince(startOfDay);

        // Sales and profit
        d.put("todaySalesCount", saleRepo.countSince(startOfDay));
        d.put("todayRevenue", todayRevenue);
        d.put("todayProfit", todayRevenue.subtract(todayCost));

        d.put("monthSalesCount", saleRepo.countSince(startOfMonth));
        d.put("monthRevenue", monthRevenue);
        d.put("monthCost", monthCost);
        d.put("monthProfit", monthRevenue.subtract(monthCost));
        d.put("cashSalesMonth", saleRepo.sumTotalByPaymentTypeSince(startOfMonth, PaymentType.PAID));
        d.put("creditSalesMonth", saleRepo.sumTotalByPaymentTypeSince(startOfMonth, PaymentType.CREDIT)
            .add(saleRepo.sumTotalByPaymentTypeSince(startOfMonth, PaymentType.PARTIAL)));

        d.put("cashReceivedToday", cashReceivedToday);
        d.put("cashReceivedMonth", cashReceivedMonth);

        BigDecimal totalOutstanding = customerBalances.stream()
            .map(m -> (BigDecimal) m.get("balance"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        d.put("totalOutstanding", totalOutstanding);
        d.put("totalCustomers", customerBalances.size());
        d.put("customersWithOutstandingBalances", customerBalances.stream()
            .filter(m -> ((BigDecimal) m.get("balance")).compareTo(BigDecimal.ZERO) > 0)
            .count());
        d.put("largestDebtor", customerBalances.stream()
            .filter(m -> ((BigDecimal) m.get("balance")).compareTo(BigDecimal.ZERO) > 0)
            .max(Comparator.comparing(m -> (BigDecimal) m.get("balance")))
            .orElse(null));

        List<Product> allProducts = productRepo.findByActiveTrue(org.springframework.data.domain.PageRequest.of(0, 10000)).getContent();
        BigDecimal inventoryValue = allProducts.stream()
            .map(p -> p.getBuyPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long lowStockCount = allProducts.stream().filter(Product::isLowStock).count();
        d.put("totalProducts", allProducts.size());
        d.put("inventoryValue", inventoryValue);
        d.put("lowStockCount", lowStockCount);

        d.put("totalDebtors", (long) customerBalances.stream()
            .filter(m -> ((BigDecimal) m.get("balance")).compareTo(BigDecimal.ZERO) > 0)
            .count());
        d.put("overdueAccounts", saleRepo.countOverdueCustomers(now.minusDays(30)));
        d.put("customersPaidThisMonth", paymentRepo.findByCreatedAtGreaterThanEqual(startOfMonth).stream()
            .map(p -> p.getCustomer().getId())
            .distinct()
            .count());

        BigDecimal totalCustomerSales = saleRepo.sumCustomerSalesTotal();
        long customersWithSales = saleRepo.countDistinctCustomersWithSales();
        d.put("averageCustomerPurchaseValue", customersWithSales > 0
            ? totalCustomerSales.divide(BigDecimal.valueOf(customersWithSales), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO);

        // Purchases
        d.put("monthPurchasesCount", purchaseRepo.countByStatusAndPurchaseDateBetween(PurchaseStatus.CONFIRMED, startOfMonth, now));
        d.put("monthPurchasesSpent", purchaseRepo.sumTotalByStatusBetween(PurchaseStatus.CONFIRMED, startOfMonth, now));
        d.put("pendingPurchaseConfirmations", purchaseRepo.countByStatus(PurchaseStatus.PENDING_REVIEW));

        // Inventory movements and recent activity
        d.put("inventoryMovementsMonth", stockMovementRepo.countBetween(startOfMonth, now));
        d.put("recentActivity", eventRepo.findTop20ByOrderByCreatedAtDesc().stream()
            .map(s -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", s.getId());
                m.put("eventType", s.getEventType());
                m.put("referenceType", s.getReferenceType());
                m.put("referenceId", s.getReferenceId());
                m.put("amount", s.getAmount());
                m.put("quantity", s.getQuantity());
                m.put("notes", s.getNotes());
                m.put("createdAt", s.getCreatedAt());
                return m;
            }).toList());

        return d;
    }

    public Map<String, Object> periodReport(String period) {
        LocalDate today = LocalDate.now();
        LocalDateTime start, end;

        switch (period.toLowerCase()) {
            case "daily":
                start = today.atStartOfDay();
                end = today.atTime(LocalTime.MAX);
                break;
            case "weekly":
                start = today.minusDays(7).atStartOfDay();
                end = today.atTime(LocalTime.MAX);
                break;
            case "monthly":
                start = today.withDayOfMonth(1).atStartOfDay();
                end = today.atTime(LocalTime.MAX);
                break;
            case "yearly":
                start = today.withDayOfYear(1).atStartOfDay();
                end = today.atTime(LocalTime.MAX);
                break;
            default:
                start = today.atStartOfDay();
                end = today.atTime(LocalTime.MAX);
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("period", period);
        r.put("startDate", start);
        r.put("endDate", end);
        r.put("salesCount", saleRepo.countSince(start));
        r.put("totalRevenue", saleRepo.sumTotalSince(start));
        r.put("totalCost", saleRepo.sumCostSince(start));
        r.put("totalProfit", saleRepo.sumTotalSince(start).subtract(saleRepo.sumCostSince(start)));

        // Top selling products
        List<Object[]> topProducts = saleRepo.topSellingProducts();
        List<Map<String, Object>> topList = new ArrayList<>();
        for (Object[] row : topProducts) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", row[0]);
            m.put("quantity", row[1]);
            topList.add(m);
        }
        r.put("topSellingProducts", topList);

        return r;
    }

    public Map<String, Object> inventoryReport() {
        List<Product> all = productRepo.findByActiveTrue(org.springframework.data.domain.PageRequest.of(0, 10000)).getContent();

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("totalProducts", all.size());
        r.put("lowStock", all.stream().filter(Product::isLowStock).count());
        r.put("outOfStock", all.stream().filter(p -> p.getStockQuantity() == 0).count());
        r.put("totalSellValue", all.stream()
            .map(p -> p.getSellPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        r.put("totalCostValue", all.stream()
            .map(p -> p.getBuyPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // By category
        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Product p : all) {
            byCategory.merge(p.getCategory(), 1L, Long::sum);
        }
        r.put("byCategory", byCategory);

        return r;
    }

    public Map<String, Object> debtReport() {
        List<Map<String, Object>> customerBalances = loadCustomerBalances();
        List<Map<String, Object>> debtors = customerBalances.stream()
            .filter(m -> ((BigDecimal) m.get("balance")).compareTo(BigDecimal.ZERO) > 0)
            .toList();

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("totalOwing", debtors.stream()
            .map(m -> (BigDecimal) m.get("balance"))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        r.put("debtorCount", debtors.size());
        r.put("debtors", debtors);

        return r;
    }

    private List<Map<String, Object>> loadCustomerBalances() {
        return customerRepo.findAll().stream().map(c -> {
            BigDecimal balance = customerLedgerService.getOutstandingBalance(c.getId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", c.getId());
            row.put("name", c.getName());
            row.put("phone", c.getPhone());
            row.put("balance", balance);
            return row;
        }).toList();
    }
}
