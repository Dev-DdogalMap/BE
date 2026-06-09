package com.ddogalmap.domain.restaurants.dto.response;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantPreviewProjection;
import java.util.List;

public record RestaurantPreviewResponse(
        Long restaurantId,
        String placeName,
        String foodType,
        String addressName,
        String imageUrl,
        Integer distance,
        Double averageScore,
        Long reviewCount,
        List<String> topTags,
        Double foodScore                 // 전체 맛집지수 (0~100, 소수 첫째자리)
) {

    public static RestaurantPreviewResponse from(
            RestaurantPreviewProjection projection,
            String imageUrl,
            List<String> topTags
    ) {
        return new RestaurantPreviewResponse(
                projection.getRestaurantId(),
                projection.getPlaceName(),
                projection.getFoodType(),
                projection.getRoadAddressName(),
                imageUrl,
                projection.getDistance(),
                projection.getAverageScore(),
                projection.getReviewCount(),
                topTags,
                projection.getFoodScore()
        );
    }
}
