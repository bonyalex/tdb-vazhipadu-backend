package com.example.vazhipadu.service;

import com.example.vazhipadu.dto.request.OrderLineRequest;
import com.example.vazhipadu.dto.request.PlaceOrderRequest;
import com.example.vazhipadu.dto.response.OrderResponse;
import com.example.vazhipadu.entity.DevoteeOrder;
import com.example.vazhipadu.entity.Offering;
import com.example.vazhipadu.entity.OrderLine;
import com.example.vazhipadu.entity.Temple;
import com.example.vazhipadu.exception.BusinessRuleException;
import com.example.vazhipadu.exception.InvalidRequestException;
import com.example.vazhipadu.exception.ResourceNotFoundException;
import com.example.vazhipadu.repository.DevoteeOrderRepository;
import com.example.vazhipadu.repository.OfferingRepository;
import com.example.vazhipadu.repository.TempleRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final ZoneId TEMPLE_ZONE = ZoneId.of("Asia/Kolkata");

    private final DevoteeOrderRepository orderRepository;
    private final OfferingRepository offeringRepository;
    private final TempleRepository templeRepository;
    private final Clock clock;

    @Transactional
    public OrderResponse place(PlaceOrderRequest request) {
        if (request.orderDate().isBefore(LocalDate.now(clock.withZone(TEMPLE_ZONE)))) {
            throw new InvalidRequestException("Order date must not be in the past");
        }
        rejectDuplicateOfferings(request.lines());
        Temple temple = templeRepository
                .findById(request.templeId())
                .orElseThrow(() -> new ResourceNotFoundException("Temple " + request.templeId() + " not found"));
        Map<Long, Offering> offerings = loadOfferings(request.lines());

        DevoteeOrder order = new DevoteeOrder();
        order.setTemple(temple);
        order.setDevoteeName(request.devoteeName().trim());
        order.setDevoteePhone(request.devoteePhone());
        order.setNakshatra(request.nakshatra());
        order.setOrderDate(request.orderDate());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineRequest lineRequest : request.lines()) {
            Offering offering = offerings.get(lineRequest.offeringId());
            ensureBookable(offering, temple);
            OrderLine line = new OrderLine();
            line.setOffering(offering);
            line.setQuantity(lineRequest.quantity());
            line.setUnitPrice(offering.getPrice());
            line.setLineTotal(offering.getPrice().multiply(BigDecimal.valueOf(lineRequest.quantity())));
            order.addLine(line);
            total = total.add(line.getLineTotal());
        }
        order.setTotal(total);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found")));
    }

    private void rejectDuplicateOfferings(List<OrderLineRequest> lines) {
        Set<Long> seen = new HashSet<>();
        for (OrderLineRequest line : lines) {
            if (!seen.add(line.offeringId())) {
                throw new InvalidRequestException("Offering " + line.offeringId() + " appears more than once");
            }
        }
    }

    private Map<Long, Offering> loadOfferings(List<OrderLineRequest> lines) {
        List<Long> ids = lines.stream().map(OrderLineRequest::offeringId).toList();
        Map<Long, Offering> offerings = offeringRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Offering::getId, Function.identity()));
        for (Long id : ids) {
            if (!offerings.containsKey(id)) {
                throw new ResourceNotFoundException("Offering " + id + " not found");
            }
        }
        return offerings;
    }

    private void ensureBookable(Offering offering, Temple temple) {
        if (!offering.getTemple().getId().equals(temple.getId())) {
            throw new BusinessRuleException("Offering " + offering.getId() + " does not belong to this temple");
        }
        if (!offering.isActive()) {
            throw new BusinessRuleException("Offering " + offering.getId() + " is not active");
        }
    }
}
