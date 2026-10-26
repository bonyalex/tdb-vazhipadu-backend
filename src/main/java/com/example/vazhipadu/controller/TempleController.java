package com.example.vazhipadu.controller;

import com.example.vazhipadu.dto.request.CreateTempleRequest;
import com.example.vazhipadu.dto.response.ApiResponse;
import com.example.vazhipadu.dto.response.TempleResponse;
import com.example.vazhipadu.service.TempleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/temples")
@RequiredArgsConstructor
public class TempleController {

    private final TempleService templeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TempleResponse> create(@Valid @RequestBody CreateTempleRequest request) {
        return ApiResponse.created(templeService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TempleResponse> get(@PathVariable Long id) {
        return ApiResponse.success(templeService.get(id));
    }
}
