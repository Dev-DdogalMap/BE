package com.ddogalmap.domain.restaurants.dto;

import java.util.List;

/**
 * 행정안전부 식품_일반음식점 조회서비스 한 페이지 응답 결과.
 */
public record GeneralRestaurantsPageResponse(
        int totalCount,
        List<GeneralRestaurantsItem> items
) {
}
