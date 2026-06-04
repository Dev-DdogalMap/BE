package com.ddogalmap.domain.restaurants.dto.response;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantInfoProjection;

import java.util.List;

public record RestaurantInfoResponse(
		Long restaurantId,
		String placeName,
		String roadAddressName,
		String phone,
		Double latitude,
		Double longitude,
		String foodType,
		String imageUrl,
		Integer distance,
		Double averageScore,
		Long reviewCount,
		List<String> topTags,
		Double foodScore
) {
	public static RestaurantInfoResponse from(
			RestaurantInfoProjection projection,
			String imageUrl,
			List<String> topTags,
			Double foodScore
	) {
		return new RestaurantInfoResponse(
				projection.getRestaurantId(),
				projection.getPlaceName(),
				projection.getRoadAddressName(),
				projection.getPhone(),
				projection.getLatitude(),
				projection.getLongitude(),
				projection.getFoodType(),
				imageUrl,
				projection.getDistance(),
				projection.getAverageScore(),
				projection.getReviewCount(),
				topTags,
				foodScore
		);
	}
}