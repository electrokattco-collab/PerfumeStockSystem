package com.perfumestock.backend.service;

import com.perfumestock.backend.entity.BusinessEvent;
import com.perfumestock.backend.entity.BusinessEventType;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.repository.BusinessEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessLedgerServiceTest {

    @Mock private BusinessEventRepository eventRepo;
    @InjectMocks private BusinessLedgerService service;

    @Test
    void recordRejectsDuplicateReference() {
        BusinessEvent existing = new BusinessEvent();
        when(eventRepo.existsByEventTypeAndReferenceTypeAndReferenceId(BusinessEventType.SALE_RECORDED, "SALE", 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.record(
            BusinessEventType.SALE_RECORDED,
            "SALE",
            10L,
            null,
            null,
            new BigDecimal("200.00"),
            1,
            "Sale recorded",
            "payload"
        )).isInstanceOf(DuplicateResourceException.class);
    }
}
