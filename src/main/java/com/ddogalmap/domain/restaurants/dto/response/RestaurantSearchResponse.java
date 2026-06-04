package com.ddogalmap.domain.restaurants.dto.response;

import java.util.List;

/**
 * 맛집 검색 페이지 응답.
 */
public record RestaurantSearchResponse(
        int page,
        int size,
        long totalCount,
        List<Item> items
) {
    public record Item(
            Long restaurantId,
            String placeName,
            String foodType,
            String addressName,
            String roadAddressName,
            Double latitude,
            Double longitude,
            Integer distance,
            Double averageScore,
            Long reviewCount,
            Integer jjinScore,
            Long bookmarkCount,
            List<String> tags
    ) {
    }
}
