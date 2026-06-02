package com.ddogalmap.domain.restaurants.dto.response;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantInfoProjection;

public record RestaurantInfoResponse(
		Long restaurantId,
		String placeName,
		String roadAddressName,
		String phone,
		String placeUrl,
		Double latitude,
		Double longitude
) {
	public static RestaurantInfoResponse from(
			RestaurantInfoProjection projection
	) {
		return new RestaurantInfoResponse(
				projection.getRestaurantId(),
				projection.getPlaceName(),
				projection.getRoadAddressName(),
				projection.getPhone(),
				projection.getPlaceUrl(),
				projection.getLatitude(),
				projection.getLongitude()
		);
	}
}