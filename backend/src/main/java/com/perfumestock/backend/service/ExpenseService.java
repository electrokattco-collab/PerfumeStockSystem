package com.perfumestock.backend.service;

import com.perfumestock.backend.service.AuditLogService;
import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.entity.Expense;
import com.perfumestock.backend.exception.ResourceNotFoundException;
import com.perfumestock.backend.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public PageResponse<Expense> getAllExpenses(Pageable pageable) {
        Page<Expense> page = expenseRepository.findAll(pageable);
        return PageResponse.of(page);
    }

    public List<Expense> getExpensesByCategory(String category) {
        return expenseRepository.findByCategoryIgnoreCase(category);
    }

    public PageResponse<Expense> getExpensesByCategory(String category, Pageable pageable) {
        Page<Expense> page = expenseRepository.findByCategoryIgnoreCase(category, pageable);
        return PageResponse.of(page);
    }

    public List<Expense> getExpensesByDateRange(LocalDateTime start, LocalDateTime end) {
        return expenseRepository.findByDateRange(start, end);
    }

    @Transactional
    public Expense createExpense(Expense e) {
        log.info("Creating expense: category={}, amount={}", e.getCategory(), e.getAmount());
        return expenseRepository.save(e);
    }

    @Transactional
    public Expense updateExpense(Long id, Expense updated) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        e.setCategory(updated.getCategory());
        e.setDescription(updated.getDescription());
        e.setAmount(updated.getAmount());
        e.setExpenseDate(updated.getExpenseDate());
        log.info("Updated expense: id={}, category={}", id, e.getCategory());
        return expenseRepository.save(e);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        log.info("Deleted expense: id={}", id);
        expenseRepository.deleteById(id);
    }

    public Double getExpensesSince(LocalDateTime start) {
        Double total = expenseRepository.sumSince(start);
        return total != null ? total : 0.0;
    }
}
