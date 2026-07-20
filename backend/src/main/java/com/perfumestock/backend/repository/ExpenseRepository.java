package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategoryIgnoreCase(String category);

    Page<Expense> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end")
    List<Expense> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end")
    Page<Expense> findByDateRange(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  Pageable pageable);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.expenseDate >= :start")
    Double sumSince(@Param("start") LocalDateTime start);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.expenseDate >= :start GROUP BY e.category")
    List<Object[]> sumByCategorySince(@Param("start") LocalDateTime start);
}
