package com.ddogalmap.domain.users.dto;

import com.ddogalmap.domain.users.enumtype.TasteExpertSortType;

public record TasteExpertSearchCondition(
        String keyword,
        String region,
        Integer minLevel,
        TasteExpertSortType sort,
        int page,
        int size
) {
}
