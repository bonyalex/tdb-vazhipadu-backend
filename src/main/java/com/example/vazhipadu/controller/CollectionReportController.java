package com.example.vazhipadu.controller;

import com.example.vazhipadu.dto.response.DailyCollectionResponse;
import com.example.vazhipadu.service.CollectionReportService;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/temples/{templeId}/collection")
@RequiredArgsConstructor
public class CollectionReportController {

    private final CollectionReportService collectionReportService;

    @GetMapping
    public DailyCollectionResponse dailyCollection(
            @PathVariable Long templeId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return collectionReportService.dailyCollection(templeId, date);
    }
}
