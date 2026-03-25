package tech.agrowerk.application.dto.cache;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public record CachedPage<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {
    public static <T> CachedPage<T> from(Page<T> page) {
        return new CachedPage<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    public Page<T> toPage() {
        return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
    }
}