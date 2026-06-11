package com.accenture.officehub_v1.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }

    public static <T> PageResponse<T> fromList(List<T> items, int page, int size) {
        int totalElements = items.size();
        int totalPages = size <= 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int safePage = Math.max(0, Math.min(page, Math.max(totalPages - 1, 0)));
        int fromIndex = Math.min(safePage * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<T> content = items.subList(fromIndex, toIndex);

        return new PageResponse<>(
                content,
                safePage,
                size,
                totalElements,
                totalPages,
                safePage == 0,
                totalPages == 0 || safePage >= totalPages - 1);
    }
}
