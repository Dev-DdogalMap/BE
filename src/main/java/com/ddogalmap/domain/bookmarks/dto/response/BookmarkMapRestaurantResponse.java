package com.ddogalmap.domain.bookmarks.dto.response;

public record BookmarkMapRestaurantResponse(
		Long bookmarkId,
		Long restaurantId,
		String placeName,
		String foodType,
		String addressName,
		Double latitude,
		Double longitude
		//TODO; 후기 이미지 가장 첫번째 이미지
		//찐 맛집 지수

) {
}