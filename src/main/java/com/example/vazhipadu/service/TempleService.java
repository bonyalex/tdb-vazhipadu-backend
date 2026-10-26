package com.example.vazhipadu.service;

import com.example.vazhipadu.dto.request.CreateTempleRequest;
import com.example.vazhipadu.dto.response.TempleResponse;
import com.example.vazhipadu.entity.Temple;
import com.example.vazhipadu.exception.ResourceNotFoundException;
import com.example.vazhipadu.repository.TempleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempleService {

    private final TempleRepository templeRepository;

    @Transactional
    public TempleResponse create(CreateTempleRequest request) {
        Temple temple = new Temple();
        temple.setName(request.name().trim());
        Temple saved = templeRepository.save(temple);
        log.info("Created temple {}", saved.getId());
        return TempleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TempleResponse get(Long id) {
        return TempleResponse.from(templeRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.warn("Temple {} not found", id);
                    return new ResourceNotFoundException("Temple " + id + " not found");
                }));
    }
}
