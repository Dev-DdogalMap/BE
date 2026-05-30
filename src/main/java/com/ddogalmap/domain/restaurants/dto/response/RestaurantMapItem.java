package com.ddogalmap.domain.restaurants.dto.response;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantMapProjection;

/**
 * 음식점 지도 Item
 */
public record RestaurantMapItem(
        Long restaurantId,
        String placeName,
        Long foodTypeId,
        String foodType,
        String addressName,
        Double latitude,
        Double longitude
) {

    public static RestaurantMapItem from(
            RestaurantMapProjection projection
    ) {
        return new RestaurantMapItem(
                projection.getRestaurantId(),
                projection.getPlaceName(),
                projection.getFoodTypeId(),
                projection.getFoodType(),
                projection.getAddressName(),
                projection.getLatitude(),
                projection.getLongitude()
        );
    }
}
