package com.example.vazhipadu.controller;

import com.example.vazhipadu.dto.request.CreateOfferingRequest;
import com.example.vazhipadu.dto.request.UpdateOfferingRequest;
import com.example.vazhipadu.dto.response.ApiResponse;
import com.example.vazhipadu.dto.response.OfferingResponse;
import com.example.vazhipadu.dto.response.PageResponse;
import com.example.vazhipadu.service.OfferingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping
    public ResponseEntity<ApiResponse<OfferingResponse>> create(@Valid @RequestBody CreateOfferingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(offeringService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OfferingResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateOfferingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfferingResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.get(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OfferingResponse>>> list(
            @RequestParam Long templeId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.listByTemple(templeId, PageRequest.of(page, size, Sort.by("id")))));
    }
}
