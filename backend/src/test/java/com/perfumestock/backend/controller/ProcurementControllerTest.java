package com.perfumestock.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.dto.ProcurementItemRequest;
import com.perfumestock.backend.dto.ProcurementRequest;
import com.perfumestock.backend.entity.Procurement;
import com.perfumestock.backend.entity.ProcurementItem;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.security.AuthEntryPointJwt;
import com.perfumestock.backend.security.AuthTokenFilter;
import com.perfumestock.backend.security.UserDetailsImpl;
import com.perfumestock.backend.security.UserDetailsServiceImpl;
import com.perfumestock.backend.service.ProcurementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcurementController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProcurementController Web MVC Tests")
class ProcurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private ProcurementService procurementService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    private ProcurementRequest validRequest;
    private Procurement savedProcurement;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "admin", "admin@example.com", "encoded",
                User.Role.ADMIN, true, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

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

        ProcurementItem item = new ProcurementItem();
        item.setId(1L);
        item.setProductName("Test Perfume");
        item.setCategory("Perfume");
        item.setQuantityPurchased(10);
        item.setBuyPrice(new BigDecimal("50.00"));
        item.setSuggestedSellingPrice(new BigDecimal("100.00"));

        savedProcurement = new Procurement();
        savedProcurement.setId(1L);
        savedProcurement.setSupplierName("Test Supplier");
        savedProcurement.setSupplierContact("0123456789");
        savedProcurement.setInvoiceNumber("INV-001");
        savedProcurement.setPurchaseDate(LocalDateTime.now());
        savedProcurement.setSubtotal(new BigDecimal("500.00"));
        savedProcurement.setVatAmount(new BigDecimal("75.00"));
        savedProcurement.setTotalAmount(new BigDecimal("575.00"));
        savedProcurement.setStatus("DRAFT");
        savedProcurement.setUploadedBy("admin");
        savedProcurement.setCreatedAt(LocalDateTime.now());
        savedProcurement.getItems().add(item);
    }

    @Test
    @DisplayName("Should create procurement")
    void shouldCreateProcurement() throws Exception {
        given(procurementService.createProcurement(any(ProcurementRequest.class), eq("admin")))
                .willReturn(savedProcurement);

        mockMvc.perform(post("/api/procurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supplierName", is("Test Supplier")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    @DisplayName("Should return 400 for invalid procurement request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        ProcurementRequest invalidRequest = new ProcurementRequest();
        invalidRequest.setItems(List.of());

        mockMvc.perform(post("/api/procurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get procurement by ID")
    void shouldGetProcurementById() throws Exception {
        given(procurementService.getProcurementById(1L)).willReturn(savedProcurement);

        mockMvc.perform(get("/api/procurements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.supplierName", is("Test Supplier")));
    }

    @Test
    @DisplayName("Should return 404 for missing procurement")
    void shouldReturn404ForMissingProcurement() throws Exception {
        given(procurementService.getProcurementById(999L))
                .willThrow(new ResourceNotFoundException("Procurement", "id", 999L));

        mockMvc.perform(get("/api/procurements/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should confirm procurement")
    void shouldConfirmProcurement() throws Exception {
        savedProcurement.setStatus("CONFIRMED");
        given(procurementService.confirmProcurement(1L, "admin")).willReturn(savedProcurement);

        mockMvc.perform(post("/api/procurements/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    @DisplayName("Should delete procurement")
    void shouldDeleteProcurement() throws Exception {
        doNothing().when(procurementService).deleteProcurement(1L, "admin");

        mockMvc.perform(delete("/api/procurements/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 for duplicate invoice number")
    void shouldReturn409ForDuplicateInvoice() throws Exception {
        given(procurementService.createProcurement(any(ProcurementRequest.class), eq("admin")))
                .willThrow(new DuplicateResourceException("Procurement", "invoiceNumber", "INV-001"));

        mockMvc.perform(post("/api/procurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should get procurement list")
    void shouldGetProcurementList() throws Exception {
        PageResponse<Procurement> page = new PageResponse<>(
                List.of(savedProcurement), 0, 20, 1, 1, true, true, false);
        given(procurementService.getAllProcurements(any())).willReturn(page);

        mockMvc.perform(get("/api/procurements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }
}
