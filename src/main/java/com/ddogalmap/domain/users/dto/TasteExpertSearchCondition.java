package com.ddogalmap.domain.users.dto;

import com.ddogalmap.domain.users.enumtype.TasteExpertSortType;

public record TasteExpertSearchCondition(
        String keyword,
        String region,
        Integer minLevel,
        Long excludedUserId,
        TasteExpertSortType sort,
        int page,
        int size
) {
}
