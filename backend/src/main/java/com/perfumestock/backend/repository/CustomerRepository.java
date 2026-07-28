package com.perfumestock.backend.repository;

import com.perfumestock.backend.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findAllByOrderByNameAsc(Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Customer> search(String q, Pageable pageable);
}
