package com.example.vazhipadu.service;

import com.example.vazhipadu.dto.response.DailyCollectionResponse;
import com.example.vazhipadu.entity.OrderStatus;
import com.example.vazhipadu.exception.ResourceNotFoundException;
import com.example.vazhipadu.repository.DevoteeOrderRepository;
import com.example.vazhipadu.repository.TempleRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionReportService {

    private final DevoteeOrderRepository orderRepository;
    private final TempleRepository templeRepository;

    @Transactional(readOnly = true)
    public DailyCollectionResponse dailyCollection(Long templeId, LocalDate date) {
        log.info("Generating daily collection report for temple: {} on date: {}", templeId, date);
        if (!templeRepository.existsById(templeId)) {
            log.warn("Rejecting collection report: temple {} not found", templeId);
            throw new ResourceNotFoundException("Temple " + templeId + " not found");
        }
        return DailyCollectionResponse.of(
                templeId, date, orderRepository.collectionByOffering(templeId, date, OrderStatus.PLACED));
    }
}
