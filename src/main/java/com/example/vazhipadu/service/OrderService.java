package com.example.vazhipadu.service;

import com.example.vazhipadu.dto.request.OrderLineRequest;
import com.example.vazhipadu.dto.request.PlaceOrderRequest;
import com.example.vazhipadu.dto.response.OrderResponse;
import com.example.vazhipadu.entity.DevoteeOrder;
import com.example.vazhipadu.entity.Offering;
import com.example.vazhipadu.entity.OrderLine;
import com.example.vazhipadu.entity.OrderStatus;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        log.info(
                "Placing order for temple: {} on date: {} with {} line(s)",
                request.templeId(),
                request.orderDate(),
                request.lines().size());
        if (request.orderDate().isBefore(LocalDate.now(clock.withZone(TEMPLE_ZONE)))) {
            log.warn(
                    "Rejecting order for temple {}: order date {} is before today",
                    request.templeId(),
                    request.orderDate());
            throw new InvalidRequestException("Order date must not be in the past");
        }
        rejectDuplicateOfferings(request.lines());
        Temple temple = templeRepository
                .findById(request.templeId())
                .orElseThrow(() -> {
                    log.warn("Rejecting order: temple {} not found", request.templeId());
                    return new ResourceNotFoundException("Temple " + request.templeId() + " not found");
                });
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
            ensureWithinDailyLimit(offering, request.orderDate(), lineRequest.quantity());
            OrderLine line = new OrderLine();
            line.setOffering(offering);
            line.setQuantity(lineRequest.quantity());
            line.setUnitPrice(offering.getPrice());
            line.setLineTotal(offering.getPrice().multiply(BigDecimal.valueOf(lineRequest.quantity())));
            order.addLine(line);
            total = total.add(line.getLineTotal());
        }
        order.setTotal(total);
        DevoteeOrder saved = orderRepository.save(order);
        log.info(
                "Successfully placed order: {} for temple: {} on date: {} with total: {}",
                saved.getId(),
                temple.getId(),
                saved.getOrderDate(),
                total);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        log.info("Retrieving order: {}", id);
        return OrderResponse.from(orderRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.warn("Order {} not found", id);
                    return new ResourceNotFoundException("Order " + id + " not found");
                }));
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        log.info("Cancelling order: {}", id);
        DevoteeOrder order = orderRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> {
                    log.warn("Rejecting cancel request: order {} not found", id);
                    return new ResourceNotFoundException("Order " + id + " not found");
                });
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Rejecting cancel request: order {} is already cancelled", id);
            throw new BusinessRuleException("Order " + id + " is already cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        log.info("Successfully cancelled order: {}", id);
        return OrderResponse.from(order);
    }

    private void rejectDuplicateOfferings(List<OrderLineRequest> lines) {
        Set<Long> seen = new HashSet<>();
        for (OrderLineRequest line : lines) {
            if (!seen.add(line.offeringId())) {
                log.warn("Rejecting order: offering {} appears more than once in the request", line.offeringId());
                throw new InvalidRequestException("Offering " + line.offeringId() + " appears more than once");
            }
        }
    }

    private Map<Long, Offering> loadOfferings(List<OrderLineRequest> lines) {
        List<Long> ids = lines.stream().map(OrderLineRequest::offeringId).toList();
        Map<Long, Offering> offerings = offeringRepository.findAllByIdForUpdate(ids).stream()
                .collect(Collectors.toMap(Offering::getId, Function.identity()));
        for (Long id : ids) {
            if (!offerings.containsKey(id)) {
                log.warn("Rejecting order: offering {} not found", id);
                throw new ResourceNotFoundException("Offering " + id + " not found");
            }
        }
        return offerings;
    }

    private void ensureWithinDailyLimit(Offering offering, LocalDate orderDate, int quantity) {
        Integer limit = offering.getDailyLimit();
        if (limit == null) {
            return;
        }
        long booked = orderRepository.sumBookedQuantity(offering.getId(), orderDate, OrderStatus.PLACED);
        if (booked + quantity > limit) {
            log.warn(
                    "Rejecting order: offering {} has a daily limit of {} with {} already booked on {}, requested {}",
                    offering.getId(),
                    limit,
                    booked,
                    orderDate,
                    quantity);
            throw new BusinessRuleException("Offering " + offering.getId() + " has only "
                    + Math.max(0, limit - booked) + " left for " + orderDate);
        }
    }

    private void ensureBookable(Offering offering, Temple temple) {
        if (!offering.getTemple().getId().equals(temple.getId())) {
            log.warn("Rejecting order: offering {} does not belong to temple {}", offering.getId(), temple.getId());
            throw new BusinessRuleException("Offering " + offering.getId() + " does not belong to this temple");
        }
        if (!offering.isActive()) {
            log.warn("Rejecting order: offering {} is not active", offering.getId());
            throw new BusinessRuleException("Offering " + offering.getId() + " is not active");
        }
    }
}
