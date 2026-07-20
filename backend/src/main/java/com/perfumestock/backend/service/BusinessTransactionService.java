package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.BusinessTransaction;
import com.perfumestock.backend.repository.BusinessTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusinessTransactionService {
    private static final Logger log = LoggerFactory.getLogger(BusinessTransactionService.class);
    private final BusinessTransactionRepository repository;

    @Autowired
    public BusinessTransactionService(BusinessTransactionRepository repository) { this.repository = repository; }

    public List<BusinessTransaction> getAll() { return repository.findAll(); }
    public List<BusinessTransaction> getByDateRange(LocalDateTime start, LocalDateTime end) { return repository.findByDateRange(start, end); }

    @Transactional
    public BusinessTransaction create(BusinessTransaction t) {
        log.info("Business transaction: {} {} = R{}", t.getTransactionType(), t.getCategory(), t.getAmount());
        return repository.save(t);
    }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }

    public Map<String, Object> getFinancialSummary() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfWeek = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("monthStipend", nvl(repository.sumByTypeSince("STIPEND", startOfMonth)));
        summary.put("monthCashInjected", nvl(repository.sumByTypeSince("CASH_INJECTED", startOfMonth)));
        summary.put("monthMoneyCollected", nvl(repository.sumByTypeSince("MONEY_COLLECTED", startOfMonth)));
        summary.put("monthOtherIncome", nvl(repository.sumByTypeSince("OTHER_INCOME", startOfMonth)));
        summary.put("monthExpenses", nvl(repository.sumByTypeSince("EXPENSE", startOfMonth)));
        summary.put("monthTransport", nvl(repository.sumByTypeSince("TRANSPORT", startOfMonth)));
        summary.put("monthMarketing", nvl(repository.sumByTypeSince("MARKETING", startOfMonth)));
        summary.put("monthRent", nvl(repository.sumByTypeSince("RENT", startOfMonth)));
        summary.put("monthUtilities", nvl(repository.sumByTypeSince("UTILITIES", startOfMonth)));
        
        double totalIncome = nvl(repository.sumByTypeSince("STIPEND", startOfMonth)) + nvl(repository.sumByTypeSince("CASH_INJECTED", startOfMonth))
                + nvl(repository.sumByTypeSince("MONEY_COLLECTED", startOfMonth)) + nvl(repository.sumByTypeSince("OTHER_INCOME", startOfMonth));
        double totalExpenses = nvl(repository.sumByTypeSince("EXPENSE", startOfMonth)) + nvl(repository.sumByTypeSince("TRANSPORT", startOfMonth))
                + nvl(repository.sumByTypeSince("MARKETING", startOfMonth)) + nvl(repository.sumByTypeSince("RENT", startOfMonth))
                + nvl(repository.sumByTypeSince("UTILITIES", startOfMonth));
        
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netPosition", totalIncome - totalExpenses);
        
        List<Object[]> byType = repository.sumByTypeGrouped(startOfMonth);
        Map<String, Double> breakdown = new HashMap<>();
        for (Object[] row : byType) {
            breakdown.put((String) row[0], ((Number) row[1]).doubleValue());
        }
        summary.put("breakdown", breakdown);
        return summary;
    }

    private double nvl(Double v) { return v != null ? v : 0.0; }
}
