package com.example.vazhipadu.controller;

import com.example.vazhipadu.dto.request.PlaceOrderRequest;
import com.example.vazhipadu.dto.response.ApiResponse;
import com.example.vazhipadu.dto.response.OrderResponse;
import com.example.vazhipadu.dto.response.OrderSummaryResponse;
import com.example.vazhipadu.dto.response.PageResponse;
import com.example.vazhipadu.entity.OrderStatus;
import com.example.vazhipadu.service.OrderSearchService;
import com.example.vazhipadu.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderSearchService orderSearchService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> place(@Valid @RequestBody PlaceOrderRequest request) {
        log.info("POST /api/orders for temple {}", request.templeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(orderService.place(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> search(
            @RequestParam Long templeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @Pattern(regexp = "[0-9]{10}", message = "must be exactly 10 digits") String phone,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(OrderSearchService.MAX_PAGE_SIZE) int size) {
        log.info("GET /api/orders for temple {} page {} size {}", templeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(orderSearchService.search(templeId, from, to, status, phone, page, size)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable Long id) {
        log.info("POST /api/orders/{}/cancel", id);
        return ResponseEntity.ok(ApiResponse.success(orderService.cancel(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable Long id) {
        log.info("GET /api/orders/{}", id);
        return ResponseEntity.ok(ApiResponse.success(orderService.get(id)));
    }
}
