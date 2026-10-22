package com.nexusmarket.domain.ports.in.model;

import java.util.List;

/**
 * Resultado paginado neutro, sin dependencia de Spring Data.
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
