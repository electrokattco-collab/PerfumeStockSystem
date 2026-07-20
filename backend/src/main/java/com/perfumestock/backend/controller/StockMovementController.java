package com.perfumestock.backend.controller;

import com.perfumestock.backend.entity.StockMovement;
import com.perfumestock.backend.service.StockMovementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
@RequestMapping("/api/stock-movements")
@Tag(name = "Stock Movements", description = "Inventory movement history")
public class StockMovementController {
    private final StockMovementService service;
    @Autowired public StockMovementController(StockMovementService service) { this.service = service; }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockMovement>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(service.getByProduct(productId));
    }
}
