package com.example.vazhipadu.service;

import com.example.vazhipadu.dto.response.DailyCollectionResponse;
import com.example.vazhipadu.entity.OrderStatus;
import com.example.vazhipadu.exception.ResourceNotFoundException;
import com.example.vazhipadu.repository.DevoteeOrderRepository;
import com.example.vazhipadu.repository.TempleRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionReportService {

    private final DevoteeOrderRepository orderRepository;
    private final TempleRepository templeRepository;

    @Transactional(readOnly = true)
    public DailyCollectionResponse dailyCollection(Long templeId, LocalDate date) {
        if (!templeRepository.existsById(templeId)) {
            throw new ResourceNotFoundException("Temple " + templeId + " not found");
        }
        return DailyCollectionResponse.of(
                templeId, date, orderRepository.collectionByOffering(templeId, date, OrderStatus.PLACED));
    }
}
