package com.gymplatform.controller.dto;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(List.copyOf(page.getContent()), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
    public static PageRequest request(Integer page, Integer size) {
        int number = page == null ? 0 : page;
        int count = size == null ? 20 : size;
        if (number < 0 || count < 1 || count > 100 || (long) number * count > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Paginación inválida");
        return PageRequest.of(number, count);
    }
}
