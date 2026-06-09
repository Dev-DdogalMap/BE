package com.ddogalmap.domain.users.dto.response;

import java.util.List;

public record TasteExpertPageResponse(
        List<TasteExpertResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
