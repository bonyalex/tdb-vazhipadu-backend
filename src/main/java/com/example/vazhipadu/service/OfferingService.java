package com.example.vazhipadu.service;

import com.example.vazhipadu.dto.request.CreateOfferingRequest;
import com.example.vazhipadu.dto.request.UpdateOfferingRequest;
import com.example.vazhipadu.dto.response.OfferingResponse;
import com.example.vazhipadu.dto.response.PageResponse;
import com.example.vazhipadu.entity.Offering;
import com.example.vazhipadu.entity.Temple;
import com.example.vazhipadu.exception.ResourceNotFoundException;
import com.example.vazhipadu.repository.OfferingRepository;
import com.example.vazhipadu.repository.TempleRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferingService {

    private static final int MONEY_SCALE = 2;

    private final OfferingRepository offeringRepository;
    private final TempleRepository templeRepository;

    @Transactional
    public OfferingResponse create(CreateOfferingRequest request) {
        Temple temple = templeRepository
                .findById(request.templeId())
                .orElseThrow(() -> {
                    log.warn("Offering create rejected: temple {} not found", request.templeId());
                    return new ResourceNotFoundException("Temple " + request.templeId() + " not found");
                });
        Offering offering = new Offering();
        offering.setTemple(temple);
        offering.setName(request.name().trim());
        offering.setPrice(normalise(request.price()));
        offering.setActive(request.active() == null || request.active());
        offering.setDailyLimit(request.dailyLimit());
        Offering saved = offeringRepository.save(offering);
        log.info("Created offering {} for temple {}", saved.getId(), temple.getId());
        return OfferingResponse.from(saved);
    }

    @Transactional
    public OfferingResponse update(Long id, UpdateOfferingRequest request) {
        Offering offering = find(id);
        offering.setName(request.name().trim());
        offering.setPrice(normalise(request.price()));
        offering.setActive(request.active());
        offering.setDailyLimit(request.dailyLimit());
        log.info("Updated offering {}", id);
        return OfferingResponse.from(offering);
    }

    @Transactional(readOnly = true)
    public OfferingResponse get(Long id) {
        return OfferingResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<OfferingResponse> listByTemple(Long templeId, Pageable pageable) {
        return PageResponse.from(offeringRepository.findByTempleId(templeId, pageable).map(OfferingResponse::from));
    }

    private Offering find(Long id) {
        return offeringRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.warn("Offering {} not found", id);
                    return new ResourceNotFoundException("Offering " + id + " not found");
                });
    }

    private BigDecimal normalise(BigDecimal price) {
        return price.setScale(MONEY_SCALE);
    }
}
