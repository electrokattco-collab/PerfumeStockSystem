package com.perfumestock.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/planning")
@Tag(name = "Stock Purchase Planning", description = "Financial planning and stock purchasing")
public class StockPurchaseController {

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Calculate purchase plan totals")
    public ResponseEntity<Map<String, Object>> calculatePlan(@RequestBody List<Map<String, Object>> items) {
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal expectedRevenue = BigDecimal.ZERO;
        int totalItems = 0;
        
        List<Map<String, Object>> calculated = new ArrayList<>();
        
        for (Map<String, Object> item : items) {
            int qty = Integer.parseInt(item.getOrDefault("quantity", "1").toString());
            BigDecimal cost = new BigDecimal(item.getOrDefault("costPerItem", "0").toString());
            BigDecimal sellPrice = new BigDecimal(item.getOrDefault("sellingPrice", "0").toString());
            
            BigDecimal lineCost = cost.multiply(BigDecimal.valueOf(qty));
            BigDecimal lineRevenue = sellPrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal profit = lineRevenue.subtract(lineCost);
            BigDecimal margin = lineRevenue.compareTo(BigDecimal.ZERO) > 0 
                ? profit.multiply(BigDecimal.valueOf(100)).divide(lineRevenue, 1, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;
            
            totalCost = totalCost.add(lineCost);
            expectedRevenue = expectedRevenue.add(lineRevenue);
            totalItems += qty;
            
            Map<String, Object> calc = new HashMap<>(item);
            calc.put("totalCost", lineCost);
            calc.put("expectedRevenue", lineRevenue);
            calc.put("expectedProfit", profit);
            calc.put("profitMargin", margin);
            calculated.add(calc);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", calculated);
        result.put("totalCost", totalCost);
        result.put("expectedRevenue", expectedRevenue);
        result.put("expectedProfit", expectedRevenue.subtract(totalCost));
        result.put("totalItems", totalItems);
        result.put("overallMargin", expectedRevenue.compareTo(BigDecimal.ZERO) > 0 
            ? expectedRevenue.subtract(totalCost).multiply(BigDecimal.valueOf(100)).divide(expectedRevenue, 1, RoundingMode.HALF_UP)
            : BigDecimal.ZERO);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Simulate next month purchase plan")
    public ResponseEntity<Map<String, Object>> simulate(@RequestBody Map<String, Object> scenario) {
        BigDecimal availableCash = new BigDecimal(scenario.getOrDefault("availableCash", "0").toString());
        BigDecimal cashInjected = new BigDecimal(scenario.getOrDefault("cashInjected", "0").toString());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> plannedPurchases = (List<Map<String, Object>>) scenario.getOrDefault("purchases", List.of());
        
        BigDecimal totalNeeded = BigDecimal.ZERO;
        for (Map<String, Object> p : plannedPurchases) {
            int qty = Integer.parseInt(p.getOrDefault("quantity", "1").toString());
            BigDecimal cost = new BigDecimal(p.getOrDefault("costPerItem", "0").toString());
            totalNeeded = totalNeeded.add(cost.multiply(BigDecimal.valueOf(qty)));
        }
        
        BigDecimal moneyAvailable = availableCash.add(cashInjected);
        BigDecimal shortfall = totalNeeded.subtract(moneyAvailable);
        BigDecimal remaining = moneyAvailable.subtract(totalNeeded);
        
        Map<String, Object> result = new HashMap<>();
        result.put("availableCash", availableCash);
        result.put("cashInjected", cashInjected);
        result.put("totalMoneyAvailable", moneyAvailable);
        result.put("totalMoneyNeeded", totalNeeded);
        result.put("shortfall", shortfall.max(BigDecimal.ZERO));
        result.put("remainingBalance", remaining.max(BigDecimal.ZERO));
        result.put("canAfford", remaining.compareTo(BigDecimal.ZERO) >= 0);
        
        return ResponseEntity.ok(result);
    }
}
