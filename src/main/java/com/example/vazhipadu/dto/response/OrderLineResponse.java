package com.example.vazhipadu.dto.response;

import com.example.vazhipadu.entity.OrderLine;
import java.math.BigDecimal;

public record OrderLineResponse(
        Long offeringId, String offeringName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {

    public static OrderLineResponse from(OrderLine line) {
        return new OrderLineResponse(
                line.getOffering().getId(),
                line.getOffering().getName(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getLineTotal());
    }
}
