package com.example.vazhipadu.service;

import static com.example.vazhipadu.repository.OrderSpecifications.dateFrom;
import static com.example.vazhipadu.repository.OrderSpecifications.dateTo;
import static com.example.vazhipadu.repository.OrderSpecifications.forTemple;
import static com.example.vazhipadu.repository.OrderSpecifications.hasPhone;
import static com.example.vazhipadu.repository.OrderSpecifications.hasStatus;

import com.example.vazhipadu.dto.response.OrderSummaryResponse;
import com.example.vazhipadu.dto.response.PageResponse;
import com.example.vazhipadu.entity.DevoteeOrder;
import com.example.vazhipadu.entity.OrderStatus;
import com.example.vazhipadu.exception.InvalidRequestException;
import com.example.vazhipadu.exception.ResourceNotFoundException;
import com.example.vazhipadu.repository.DevoteeOrderRepository;
import com.example.vazhipadu.repository.TempleRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderSearchService {

    public static final int MAX_PAGE_SIZE = 100;

    private static final Sort STABLE_ORDER = Sort.by(Sort.Order.desc("orderDate"), Sort.Order.desc("id"));

    private final DevoteeOrderRepository orderRepository;
    private final TempleRepository templeRepository;

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> search(
            Long templeId, LocalDate from, LocalDate to, OrderStatus status, String phone, int page, int size) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException("from must not be after to");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!templeRepository.existsById(templeId)) {
            throw new ResourceNotFoundException("Temple " + templeId + " not found");
        }
        Specification<DevoteeOrder> filter =
                forTemple(templeId).and(dateFrom(from)).and(dateTo(to)).and(hasStatus(status)).and(hasPhone(phone));
        return PageResponse.from(orderRepository
                .findAll(filter, PageRequest.of(page, size, STABLE_ORDER))
                .map(OrderSummaryResponse::from));
    }
}
