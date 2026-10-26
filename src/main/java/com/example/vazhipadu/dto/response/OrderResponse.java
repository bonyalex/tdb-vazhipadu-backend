package com.example.vazhipadu.dto.response;

import com.example.vazhipadu.entity.DevoteeOrder;
import com.example.vazhipadu.entity.Nakshatra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        Long id,
        Long templeId,
        String devoteeName,
        String devoteePhone,
        Nakshatra nakshatra,
        LocalDate orderDate,
        BigDecimal total,
        List<OrderLineResponse> lines) {

    public static OrderResponse from(DevoteeOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getTemple().getId(),
                order.getDevoteeName(),
                order.getDevoteePhone(),
                order.getNakshatra(),
                order.getOrderDate(),
                order.getTotal(),
                order.getLines().stream().map(OrderLineResponse::from).toList());
    }
}
