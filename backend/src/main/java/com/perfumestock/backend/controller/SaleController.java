package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.dto.SaleRequest;
import com.perfumestock.backend.dto.SaleResponse;
import com.perfumestock.backend.entity.Sale;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.security.UserDetailsImpl;
import com.perfumestock.backend.service.SaleService;
import com.perfumestock.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "Record, update, and manage sales")
public class SaleController {

    private final SaleService saleService;
    private final UserService userService;

    @Autowired
    public SaleController(SaleService saleService, UserService userService) {
        this.saleService = saleService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    public ResponseEntity<?> getAllSales(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String customer,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sort));

        PageResponse<Sale> salePage;
        if (name != null || customer != null) {
            salePage = saleService.searchSales(name, customer, pageable);
        } else {
            salePage = saleService.getAllSales(pageable);
        }

        PageResponse<SaleResponse> response = mapSalePage(salePage);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    public ResponseEntity<?> recordSale(
            @Valid @RequestBody SaleRequest request,
            Authentication authentication) {
        User user = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            user = userService.getUserById(((UserDetailsImpl) authentication.getPrincipal()).getId());
        }
        var sale = saleService.recordSale(request, user);
        return ResponseEntity.ok(SaleResponse.fromEntity(sale));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SaleResponse> updateSale(
            @PathVariable Long id,
            @Valid @RequestBody SaleRequest request) {
        var sale = saleService.updateSale(id, request);
        return ResponseEntity.ok(SaleResponse.fromEntity(sale));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<SaleResponse> markAsPaid(@PathVariable Long id) {
        var sale = saleService.markAsPaid(id);
        return ResponseEntity.ok(SaleResponse.fromEntity(sale));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return ResponseEntity.ok().build();
    }

    private PageResponse<SaleResponse> mapSalePage(PageResponse<Sale> page) {
        List<SaleResponse> content = page.getContent().stream()
                .map(SaleResponse::fromEntity)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content, page.getPage(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}
