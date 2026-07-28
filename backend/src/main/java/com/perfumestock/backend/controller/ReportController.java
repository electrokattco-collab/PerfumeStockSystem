package com.perfumestock.backend.controller;

import com.perfumestock.backend.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(reportService.dashboard());
    }

    @GetMapping("/period/{period}")
    public ResponseEntity<Map<String, Object>> period(@PathVariable String period) {
        return ResponseEntity.ok(reportService.periodReport(period));
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventory() {
        return ResponseEntity.ok(reportService.inventoryReport());
    }

    @GetMapping("/debt")
    public ResponseEntity<Map<String, Object>> debt() {
        return ResponseEntity.ok(reportService.debtReport());
    }
}
