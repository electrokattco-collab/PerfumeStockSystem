package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.Product;
import com.perfumestock.backend.entity.StockMovement;
import com.perfumestock.backend.repository.ProductRepository;
import com.perfumestock.backend.repository.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class StockMovementService {
    private static final Logger log = LoggerFactory.getLogger(StockMovementService.class);
    private final StockMovementRepository repository;
    private final ProductRepository productRepository;

    @Autowired
    public StockMovementService(StockMovementRepository repository, ProductRepository productRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
    }

    public List<StockMovement> getByProduct(Long productId) { return repository.findByProductIdOrderByCreatedAtDesc(productId); }

    @Transactional
    public StockMovement record(Long productId, String type, int quantity, BigDecimal unitCost, Long refId, String refType, String notes, String user) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;
        StockMovement sm = new StockMovement(product, type, quantity, unitCost, user);
        sm.setReferenceId(refId);
        sm.setReferenceType(refType);
        sm.setNotes(notes);
        log.info("Stock movement: {} {} x{} for product {}", type, quantity, product.getName());
        return repository.save(sm);
    }
}
