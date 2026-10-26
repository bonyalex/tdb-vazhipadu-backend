package com.example.vazhipadu.controller;

import com.example.vazhipadu.dto.request.CreateTempleRequest;
import com.example.vazhipadu.dto.response.ApiResponse;
import com.example.vazhipadu.dto.response.TempleResponse;
import com.example.vazhipadu.service.TempleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/temples")
@RequiredArgsConstructor
public class TempleController {

    private final TempleService templeService;

    @PostMapping
    public ResponseEntity<ApiResponse<TempleResponse>> create(@Valid @RequestBody CreateTempleRequest request) {
        log.info("POST /api/temples");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(templeService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TempleResponse>> get(@PathVariable Long id) {
        log.info("GET /api/temples/{}", id);
        return ResponseEntity.ok(ApiResponse.success(templeService.get(id)));
    }
}
