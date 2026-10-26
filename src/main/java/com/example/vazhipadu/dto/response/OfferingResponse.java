package com.example.vazhipadu.dto.response;

import com.example.vazhipadu.entity.Offering;
import java.math.BigDecimal;

public record OfferingResponse(Long id, Long templeId, String name, BigDecimal price, boolean active) {

    public static OfferingResponse from(Offering offering) {
        return new OfferingResponse(
                offering.getId(),
                offering.getTemple().getId(),
                offering.getName(),
                offering.getPrice(),
                offering.isActive());
    }
}
