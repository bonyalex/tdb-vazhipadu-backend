package com.example.vazhipadu.service;

import com.example.vazhipadu.dto.request.CreateTempleRequest;
import com.example.vazhipadu.dto.response.TempleResponse;
import com.example.vazhipadu.entity.Temple;
import com.example.vazhipadu.exception.ResourceNotFoundException;
import com.example.vazhipadu.repository.TempleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TempleService {

    private final TempleRepository templeRepository;

    @Transactional
    public TempleResponse create(CreateTempleRequest request) {
        Temple temple = new Temple();
        temple.setName(request.name().trim());
        return TempleResponse.from(templeRepository.save(temple));
    }

    @Transactional(readOnly = true)
    public TempleResponse get(Long id) {
        return TempleResponse.from(templeRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Temple " + id + " not found")));
    }
}
