package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.MessageResponse;
import com.perfumestock.backend.dto.SaleRequest;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.security.UserDetailsImpl;
import com.perfumestock.backend.service.SaleService;
import com.perfumestock.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SaleController {

    private final SaleService saleService;
    private final UserService userService;

    @Autowired
    public SaleController(SaleService saleService, UserService userService) {
        this.saleService = saleService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Sale>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    public ResponseEntity<?> recordSale(@Valid @RequestBody SaleRequest request, 
                                        Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            
            Sale sale = saleService.recordSale(request, user);
            return ResponseEntity.ok(sale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Sale>> searchSales(@RequestParam String productName) {
        return ResponseEntity.ok(saleService.searchSalesByProductName(productName));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Sale>> getSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(saleService.getSalesByDateRange(startDate, endDate));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    public ResponseEntity<List<Sale>> getTodaySales() {
        return ResponseEntity.ok(saleService.getTodaySales());
    }
}
