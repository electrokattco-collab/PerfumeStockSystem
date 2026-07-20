package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.ProductResponse;
import com.perfumestock.backend.dto.SaleResponse;
import com.perfumestock.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Dashboard, profit, and inventory reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        return ResponseEntity.ok(reportService.getDashboardSummary());
    }

    @GetMapping("/profit")
    public ResponseEntity<Map<String, Object>> getProfitReport() {
        return ResponseEntity.ok(reportService.getProfitReport());
    }

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyReport() {
        return ResponseEntity.ok(reportService.getDailyReport());
    }

    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyReport() {
        return ResponseEntity.ok(reportService.getWeeklyReport());
    }

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyReport() {
        return ResponseEntity.ok(reportService.getMonthlyReport());
    }

    @GetMapping("/lowstock")
    public ResponseEntity<List<ProductResponse>> getLowStockReport() {
        List<ProductResponse> products = reportService.getLowStockReport().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/export")
    public ResponseEntity<List<SaleResponse>> exportSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<SaleResponse> sales = reportService.getSalesCsv(start, end).stream()
                .map(SaleResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sales);
    }
}
