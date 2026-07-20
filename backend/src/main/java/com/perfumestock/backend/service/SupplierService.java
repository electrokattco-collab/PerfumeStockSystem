package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.entity.Supplier;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SupplierService {
    private static final Logger log = LoggerFactory.getLogger(SupplierService.class);
    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public PageResponse<Supplier> getAll(Pageable pageable) {
        return PageResponse.of(supplierRepository.findAll(pageable));
    }
    public List<Supplier> getActive() { return supplierRepository.findByActiveTrue(); }
    public Supplier getById(Long id) { return supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id)); }
    public List<Supplier> search(String name) { return supplierRepository.findByNameContainingIgnoreCase(name); }

    @Transactional
    public Supplier create(Supplier s) {
        log.info("Created supplier: {}", s.getName());
        return supplierRepository.save(s);
    }

    @Transactional
    public Supplier update(Long id, Supplier updated) {
        Supplier s = getById(id);
        s.setName(updated.getName());
        s.setPhone(updated.getPhone());
        s.setEmail(updated.getEmail());
        s.setAddress(updated.getAddress());
        s.setNotes(updated.getNotes());
        s.setActive(updated.getActive());
        return supplierRepository.save(s);
    }

    @Transactional
    public void delete(Long id) {
        Supplier s = getById(id);
        s.setActive(false);
        supplierRepository.save(s);
    }
}
