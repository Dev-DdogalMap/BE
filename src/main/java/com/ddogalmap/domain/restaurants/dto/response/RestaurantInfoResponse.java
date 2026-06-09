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
		Double foodScore,                // 전체 맛집지수 (0~100, 소수 첫째자리)
		Integer residentRecommendRate,   // 주민 추천 비율 (0~100)
		Integer revisitRate,             // 재방문율 (0~100)
		Long visitVerifyCount,           // 방문 인증 수
		Long bookmarkCount               // 즐겨찾기 수
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
				projection.getFoodScore(),
				projection.getResidentRecommendRate(),
				projection.getRevisitRate(),
				projection.getVisitVerifyCount(),
				projection.getBookmarkCount()
		);
	}
}