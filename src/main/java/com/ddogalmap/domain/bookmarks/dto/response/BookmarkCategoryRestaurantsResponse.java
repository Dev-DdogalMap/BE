package com.ddogalmap.domain.bookmarks.dto.response;


import java.util.List;

public record BookmarkCategoryRestaurantsResponse(
		Long bookmarkCategoryId,
		String bookmarkCategoryName,
		Integer bookmarkCount,
		List<BookmarkMapRestaurantResponse> restaurants
) {
}