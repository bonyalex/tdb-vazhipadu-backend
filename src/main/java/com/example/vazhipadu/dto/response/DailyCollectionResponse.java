package com.example.vazhipadu.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyCollectionResponse(
        Long templeId, LocalDate date, BigDecimal totalAmount, long totalCount, List<OfferingCollectionResponse> offerings) {

    public static DailyCollectionResponse of(Long templeId, LocalDate date, List<OfferingCollectionResponse> offerings) {
        BigDecimal totalAmount =
                offerings.stream().map(OfferingCollectionResponse::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalCount = offerings.stream().mapToLong(OfferingCollectionResponse::count).sum();
        return new DailyCollectionResponse(templeId, date, totalAmount, totalCount, offerings);
    }
}
