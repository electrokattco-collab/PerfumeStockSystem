package com.perfumestock.backend.controller;

import com.perfumestock.backend.service.EnhancedReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/reports")
@Tag(name = "Enhanced Reports", description = "Role-based dashboards and analytics")
public class EnhancedReportController {
    private final EnhancedReportService service;
    @Autowired public EnhancedReportController(EnhancedReportService service) { this.service = service; }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminDashboard() { return ResponseEntity.ok(service.getAdminDashboard()); }

    @GetMapping("/manager/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> managerDashboard() { return ResponseEntity.ok(service.getManagerDashboard()); }

    @GetMapping("/sales/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    public ResponseEntity<Map<String, Object>> salesDashboard() { return ResponseEntity.ok(service.getSalesDashboard()); }

    @GetMapping("/trend")
    public ResponseEntity<List<Map<String, Object>>> salesTrend() { return ResponseEntity.ok(service.getSalesTrend()); }

    @GetMapping("/expenses/breakdown")
    public ResponseEntity<List<Map<String, Object>>> expenseBreakdown() { return ResponseEntity.ok(service.getExpenseBreakdown()); }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryReport() { return ResponseEntity.ok(service.getInventoryReport()); }

    @GetMapping("/debt")
    public ResponseEntity<Map<String, Object>> debtReport() { return ResponseEntity.ok(service.getDebtReport()); }

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> daily() { return ResponseEntity.ok(service.getDailyReport()); }

    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> weekly() { return ResponseEntity.ok(service.getWeeklyReport()); }

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> monthly() { return ResponseEntity.ok(service.getMonthlyReport()); }

    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> yearly() { return ResponseEntity.ok(service.getYearlyReport()); }
}
