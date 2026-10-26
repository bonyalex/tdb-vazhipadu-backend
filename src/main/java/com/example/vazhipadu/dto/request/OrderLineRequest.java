package com.example.vazhipadu.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(@NotNull Long offeringId, @NotNull @Min(1) @Max(1000) Integer quantity) {}
