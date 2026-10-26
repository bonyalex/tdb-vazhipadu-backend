package com.example.vazhipadu.dto.response;

import com.example.vazhipadu.exception.ApiError;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(int status, boolean success, T data, ApiError error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), true, data, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(HttpStatus.CREATED.value(), true, data, null);
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return new ApiResponse<>(error.status(), false, null, error);
    }
}
