package com.ddogalmap.domain.restaurants.dto.response;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantMapProjection;

import java.util.List;

/**
 * 음식점 지도 응답 DTO
 */
public record RestaurantMapResponse(
        List<RestaurantMapItem> restaurants
) {
    public static RestaurantMapResponse from(List<RestaurantMapProjection> restaurants) {
        return new RestaurantMapResponse(
                restaurants.stream()
                        .map(RestaurantMapItem::from)
                        .toList()
        );
    }
}
