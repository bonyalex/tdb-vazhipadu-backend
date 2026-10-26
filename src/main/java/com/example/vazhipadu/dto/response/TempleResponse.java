package com.example.vazhipadu.dto.response;

import com.example.vazhipadu.entity.Temple;

public record TempleResponse(Long id, String name) {

    public static TempleResponse from(Temple temple) {
        return new TempleResponse(temple.getId(), temple.getName());
    }
}
