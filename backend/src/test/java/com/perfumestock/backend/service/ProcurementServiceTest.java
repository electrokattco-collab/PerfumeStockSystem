package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.dto.ProcurementItemRequest;
import com.perfumestock.backend.dto.ProcurementRequest;
import com.perfumestock.backend.entity.Procurement;
import com.perfumestock.backend.entity.ProcurementItem;
import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.exception.BusinessRuleException;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.ProcurementRepository;
import com.perfumestock.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcurementService Unit Tests")
class ProcurementServiceTest {

    @Mock
    private ProcurementRepository procurementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private ProcurementService procurementService;

    private ProcurementRequest validRequest;
    private Procurement savedProcurement;

    @BeforeEach
    void setUp() {
        ProcurementItemRequest itemReq = new ProcurementItemRequest();
        itemReq.setProductName("Test Perfume");
        itemReq.setCategory("Perfume");
        itemReq.setQuantityPurchased(10);
        itemReq.setBuyPrice(new BigDecimal("50.00"));
        itemReq.setSuggestedSellingPrice(new BigDecimal("100.00"));

        validRequest = new ProcurementRequest();
        validRequest.setSupplierName("Test Supplier");
        validRequest.setSupplierContact("0123456789");
        validRequest.setInvoiceNumber("INV-001");
        validRequest.setPurchaseDate(LocalDateTime.now());
        validRequest.setVatAmount(new BigDecimal("75.00"));
        validRequest.setNotes("Test notes");
        validRequest.setItems(List.of(itemReq));

        savedProcurement = new Procurement();
        savedProcurement.setId(1L);
        savedProcurement.setSupplierName("Test Supplier");
        savedProcurement.setSupplierContact("0123456789");
        savedProcurement.setInvoiceNumber("INV-001");
        savedProcurement.setPurchaseDate(LocalDateTime.now());
        savedProcurement.setVatAmount(new BigDecimal("75.00"));
        savedProcurement.setStatus("DRAFT");
        savedProcurement.setUploadedBy("admin");
    }

    @Test
    @DisplayName("Should create procurement successfully")
    void shouldCreateProcurement() {
        given(procurementRepository.existsByInvoiceNumber("INV-001")).willReturn(false);
        given(procurementRepository.save(any(Procurement.class))).willReturn(savedProcurement);

        Procurement result = procurementService.createProcurement(validRequest, "admin");

        assertThat(result).isNotNull();
        assertThat(result.getSupplierName()).isEqualTo("Test Supplier");
        verify(procurementRepository).save(any(Procurement.class));
        verify(auditLogService).log(eq("Procurement"), eq(1L), eq("CREATED"), anyString(), isNull(), eq("admin"));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for duplicate invoice number")
    void shouldThrowForDuplicateInvoice() {
        given(procurementRepository.existsByInvoiceNumber("INV-001")).willReturn(true);

        assertThatThrownBy(() -> procurementService.createProcurement(validRequest, "admin"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("invoiceNumber");
    }

    @Test
    @DisplayName("Should get procurement by ID")
    void shouldGetProcurementById() {
        given(procurementRepository.findById(1L)).willReturn(Optional.of(savedProcurement));

        Procurement result = procurementService.getProcurementById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for missing procurement")
    void shouldThrowForMissingProcurement() {
        given(procurementRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> procurementService.getProcurementById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete draft procurement")
    void shouldDeleteDraftProcurement() {
        given(procurementRepository.findById(1L)).willReturn(Optional.of(savedProcurement));

        procurementService.deleteProcurement(1L, "admin");

        verify(procurementRepository).delete(savedProcurement);
        verify(auditLogService).log(eq("Procurement"), eq(1L), eq("DELETED"), anyString(), isNull(), eq("admin"));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when deleting confirmed procurement")
    void shouldThrowWhenDeletingConfirmed() {
        savedProcurement.setStatus("CONFIRMED");
        given(procurementRepository.findById(1L)).willReturn(Optional.of(savedProcurement));

        assertThatThrownBy(() -> procurementService.deleteProcurement(1L, "admin"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("confirmed");
    }

    @Test
    @DisplayName("Should confirm procurement and create product")
    void shouldConfirmProcurement() {
        ProcurementItem item = new ProcurementItem();
        item.setProductName("Test Perfume");
        item.setCategory("Perfume");
        item.setQuantityPurchased(10);
        item.setBuyPrice(new BigDecimal("50.00"));
        item.setSuggestedSellingPrice(new BigDecimal("100.00"));
        item.setProcurement(savedProcurement);
        savedProcurement.getItems().add(item);

        Product savedProduct = new Product();
        savedProduct.setId(10L);

        given(procurementRepository.findById(1L)).willReturn(Optional.of(savedProcurement));
        given(productRepository.findByNameContainingIgnoreCase("Test Perfume")).willReturn(List.of());
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);
        given(procurementRepository.save(any(Procurement.class))).willReturn(savedProcurement);

        Procurement result = procurementService.confirmProcurement(1L, "admin");

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(productRepository).save(any(Product.class));
        verify(stockMovementService).record(eq(10L), eq("PURCHASE"), eq(10), any(BigDecimal.class), eq(1L), eq("PROCUREMENT"), anyString(), eq("admin"));
    }

    @Test
    @DisplayName("Should confirm and update existing product stock")
    void shouldConfirmAndUpdateExistingProduct() {
        ProcurementItem item = new ProcurementItem();
        item.setProductName("Existing Perfume");
        item.setCategory("Perfume");
        item.setQuantityPurchased(5);
        item.setBuyPrice(new BigDecimal("40.00"));
        item.setSuggestedSellingPrice(new BigDecimal("90.00"));
        item.setProcurement(savedProcurement);
        savedProcurement.getItems().add(item);

        Product existingProduct = new Product();
        existingProduct.setId(5L);
        existingProduct.setName("Existing Perfume");
        existingProduct.setStockQuantity(10);
        existingProduct.setBuyPrice(new BigDecimal("35.00"));
        existingProduct.setSellPrice(new BigDecimal("80.00"));

        given(procurementRepository.findById(1L)).willReturn(Optional.of(savedProcurement));
        given(productRepository.findByNameContainingIgnoreCase("Existing Perfume")).willReturn(List.of(existingProduct));
        given(productRepository.save(any(Product.class))).willReturn(existingProduct);
        given(procurementRepository.save(any(Procurement.class))).willReturn(savedProcurement);

        Procurement result = procurementService.confirmProcurement(1L, "admin");

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(existingProduct.getStockQuantity()).isEqualTo(15);
        assertThat(existingProduct.getBuyPrice()).isEqualByComparingTo(new BigDecimal("40.00"));
        verify(stockMovementService).record(eq(5L), eq("PURCHASE"), eq(5), any(BigDecimal.class), eq(1L), eq("PROCUREMENT"), anyString(), eq("admin"));
    }

    @Test
    @DisplayName("Should throw when confirming already confirmed procurement")
    void shouldThrowWhenConfirmingConfirmed() {
        savedProcurement.setStatus("CONFIRMED");
        given(procurementRepository.findById(1L)).willReturn(Optional.of(savedProcurement));

        assertThatThrownBy(() -> procurementService.confirmProcurement(1L, "admin"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already confirmed");
    }

    @Test
    @DisplayName("Should return dashboard stats")
    void shouldReturnDashboardStats() {
        given(procurementRepository.countByPurchaseDateRange(any(), any())).willReturn(5L);
        given(procurementRepository.sumTotalAmountByDateRange(any(), any())).willReturn(new BigDecimal("1000.00"));
        given(procurementRepository.countDistinctSuppliers()).willReturn(3L);
        given(procurementRepository.findTop10ByOrderByCreatedAtDesc()).willReturn(List.of());

        Map<String, Object> stats = procurementService.getDashboardStats();

        assertThat(stats).containsKey("todayPurchases");
        assertThat(stats).containsKey("monthPurchases");
        assertThat(stats).containsKey("monthCost");
        assertThat(stats).containsKey("supplierCount");
        assertThat(stats).containsKey("recentProcurements");
    }

    @Test
    @DisplayName("Should search procurements")
    void shouldSearchProcurements() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Procurement> page = new PageImpl<>(List.of(savedProcurement), pageable, 1);
        given(procurementRepository.search("Test", null, null, pageable)).willReturn(page);

        PageResponse<Procurement> result = procurementService.searchProcurements("Test", null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
