package com.example.vazhipadu.dto.request;

import com.example.vazhipadu.entity.Nakshatra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record PlaceOrderRequest(
        @NotNull Long templeId,
        @NotBlank @Size(max = 100) String devoteeName,
        @NotBlank @Pattern(regexp = "[0-9]{10}", message = "must be exactly 10 digits") String devoteePhone,
        @NotNull Nakshatra nakshatra,
        @NotNull LocalDate orderDate,
        @NotEmpty @Valid List<OrderLineRequest> lines) {}
