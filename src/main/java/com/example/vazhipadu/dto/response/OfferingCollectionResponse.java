package com.example.vazhipadu.dto.response;

import java.math.BigDecimal;

public record OfferingCollectionResponse(Long offeringId, String offeringName, long count, BigDecimal amount) {}
