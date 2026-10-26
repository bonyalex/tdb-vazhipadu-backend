package com.example.vazhipadu.controller;

import com.example.vazhipadu.dto.request.CreateOfferingRequest;
import com.example.vazhipadu.dto.request.UpdateOfferingRequest;
import com.example.vazhipadu.dto.response.OfferingResponse;
import com.example.vazhipadu.dto.response.PageResponse;
import com.example.vazhipadu.service.OfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferingResponse create(@Valid @RequestBody CreateOfferingRequest request) {
        return offeringService.create(request);
    }

    @PutMapping("/{id}")
    public OfferingResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOfferingRequest request) {
        return offeringService.update(id, request);
    }

    @GetMapping("/{id}")
    public OfferingResponse get(@PathVariable Long id) {
        return offeringService.get(id);
    }

    @GetMapping
    public PageResponse<OfferingResponse> list(
            @RequestParam Long templeId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return offeringService.listByTemple(templeId, pageable);
    }
}
