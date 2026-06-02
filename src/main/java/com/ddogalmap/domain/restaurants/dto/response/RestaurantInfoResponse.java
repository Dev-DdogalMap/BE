package com.ddogalmap.domain.restaurants.dto.response;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantInfoProjection;

public record RestaurantInfoResponse(
		Long restaurantId,
		String roadAddressName,
		String phone,
		String placeUrl,
		Double x,
		Double y
) {
	public static RestaurantInfoResponse from(
			RestaurantInfoProjection projection
	) {
		return new RestaurantInfoResponse(
				projection.getRestaurantId(),
				projection.getRoadAddressName(),
				projection.getPhone(),
				projection.getPlaceUrl(),
				projection.getX(),
				projection.getY()
		);
	}
}