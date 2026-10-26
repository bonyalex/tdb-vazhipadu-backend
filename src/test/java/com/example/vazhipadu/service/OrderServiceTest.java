package com.example.vazhipadu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.vazhipadu.dto.request.OrderLineRequest;
import com.example.vazhipadu.dto.request.PlaceOrderRequest;
import com.example.vazhipadu.dto.response.OrderResponse;
import com.example.vazhipadu.entity.DevoteeOrder;
import com.example.vazhipadu.entity.Nakshatra;
import com.example.vazhipadu.entity.Offering;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-26T06:00:00Z");
    private static final LocalDate TODAY_IST = LocalDate.of(2026, 10, 26);

    @Mock
    private DevoteeOrderRepository orderRepository;

    @Mock
    private OfferingRepository offeringRepository;

    @Mock
    private TempleRepository templeRepository;

    private OrderService orderService;
    private Temple temple;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository, offeringRepository, templeRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        temple = temple(1L);
    }

    @Test
    void totalIsSumOfQuantityTimesPriceAcrossLines() {
        Offering pushpanjali = offering(10L, temple, "30.00", true);
        Offering archana = offering(11L, temple, "50.00", true);
        when(templeRepository.findById(1L)).thenReturn(Optional.of(temple));
        when(offeringRepository.findAllByIdForUpdate(List.of(10L, 11L))).thenReturn(List.of(pushpanjali, archana));
        when(orderRepository.save(any(DevoteeOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PlaceOrderRequest request =
                request(TODAY_IST, new OrderLineRequest(10L, 2), new OrderLineRequest(11L, 1));

        OrderResponse response = orderService.place(request);

        assertEquals(0, new BigDecimal("110").compareTo(response.total()));
        assertEquals(2, response.lines().size());
        assertEquals(0, new BigDecimal("60").compareTo(response.lines().get(0).lineTotal()));
    }

    @Test
    void orderLineSnapshotsOfferingPriceAtBookingTime() {
        Offering pushpanjali = offering(10L, temple, "30.00", true);
        when(templeRepository.findById(1L)).thenReturn(Optional.of(temple));
        when(offeringRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of(pushpanjali));
        when(orderRepository.save(any(DevoteeOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.place(request(TODAY_IST, new OrderLineRequest(10L, 1)));
        pushpanjali.setPrice(new BigDecimal("99.00"));

        assertEquals(0, new BigDecimal("30").compareTo(response.lines().get(0).unitPrice()));
    }

    @Test
    void orderForInactiveOfferingIsRejected() {
        Offering inactive = offering(10L, temple, "30.00", false);
        when(templeRepository.findById(1L)).thenReturn(Optional.of(temple));
        when(offeringRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of(inactive));

        assertThrows(
                BusinessRuleException.class,
                () -> orderService.place(request(TODAY_IST, new OrderLineRequest(10L, 1))));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void orderForOfferingOfAnotherTempleIsRejected() {
        Offering foreign = offering(10L, temple(2L), "30.00", true);
        when(templeRepository.findById(1L)).thenReturn(Optional.of(temple));
        when(offeringRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of(foreign));

        assertThrows(
                BusinessRuleException.class,
                () -> orderService.place(request(TODAY_IST, new OrderLineRequest(10L, 1))));
    }

    @Test
    void orderForUnknownOfferingIsNotFound() {
        when(templeRepository.findById(1L)).thenReturn(Optional.of(temple));
        when(offeringRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.place(request(TODAY_IST, new OrderLineRequest(10L, 1))));
    }

    @Test
    void orderDatedBeforeTodayInIndiaIsRejected() {
        assertThrows(
                InvalidRequestException.class,
                () -> orderService.place(request(TODAY_IST.minusDays(1), new OrderLineRequest(10L, 1))));
    }

    @Test
    void repeatedOfferingInOneOrderIsRejected() {
        assertThrows(
                InvalidRequestException.class,
                () -> orderService.place(
                        request(TODAY_IST, new OrderLineRequest(10L, 1), new OrderLineRequest(10L, 2))));
    }

    @Test
    void unknownOrderIsNotFound() {
        when(orderRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.get(5L));
    }

    @Test
    void orderForTwoIsRejectedWhenFourOfFiveAreBooked() {
        Offering limited = offering(10L, temple, "30.00", true);
        limited.setDailyLimit(5);
        when(templeRepository.findById(1L)).thenReturn(Optional.of(temple));
        when(offeringRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of(limited));
        when(orderRepository.sumBookedQuantity(10L, TODAY_IST, OrderStatus.PLACED)).thenReturn(4L);
        PlaceOrderRequest request = request(TODAY_IST, new OrderLineRequest(10L, 2));

        assertThrows(BusinessRuleException.class, () -> orderService.place(request));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void orderForOneSucceedsWhenFourOfFiveAreBooked() {
        Offering limited = offering(10L, temple, "30.00", true);
        limited.setDailyLimit(5);
        when(templeRepository.findById(1L)).thenReturn(Optional.of(temple));
        when(offeringRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of(limited));
        when(orderRepository.sumBookedQuantity(10L, TODAY_IST, OrderStatus.PLACED)).thenReturn(4L);
        when(orderRepository.save(any(DevoteeOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.place(request(TODAY_IST, new OrderLineRequest(10L, 1)));

        assertEquals(1, response.lines().size());
    }

    @Test
    void offeringWithoutDailyLimitNeverChecksBookedQuantity() {
        Offering unlimited = offering(10L, temple, "30.00", true);
        when(templeRepository.findById(1L)).thenReturn(Optional.of(temple));
        when(offeringRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of(unlimited));
        when(orderRepository.save(any(DevoteeOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.place(request(TODAY_IST, new OrderLineRequest(10L, 500)));

        verify(orderRepository, never()).sumBookedQuantity(any(), any(), any());
    }

    private PlaceOrderRequest request(LocalDate date, OrderLineRequest... lines) {
        return new PlaceOrderRequest(1L, "Anil Kumar", "9876543210", Nakshatra.ROHINI, date, List.of(lines));
    }

    private Temple temple(Long id) {
        Temple result = new Temple();
        result.setId(id);
        result.setName("Temple " + id);
        return result;
    }

    private Offering offering(Long id, Temple owner, String price, boolean active) {
        Offering offering = new Offering();
        offering.setId(id);
        offering.setTemple(owner);
        offering.setName("Offering " + id);
        offering.setPrice(new BigDecimal(price));
        offering.setActive(active);
        return offering;
    }
}
