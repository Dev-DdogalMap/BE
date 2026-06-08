package com.ddogalmap.domain.bookmarks.service;

import com.ddogalmap.domain.bookmarks.dto.response.BookmarkCategoryResponse;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkCategoryRestaurantsResponse;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkCategoryStatusResponse;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkRestaurantResponse;

import java.util.List;

//조회 전답 READ
public interface BookmarkQueryService {

	List<BookmarkCategoryResponse> getMyBookmarkCategories(Long userId);

	List<BookmarkRestaurantResponse> getMyBookmarks(Long userId);

	List<BookmarkRestaurantResponse> getMyBookmarksByCategory(
			Long userId,
			Long bookmarkCategoryId
	);

	List<BookmarkCategoryStatusResponse> getBookmarkCategoryStatuses(
			Long userId,
			Long restaurantId
	);

	BookmarkCategoryRestaurantsResponse getBookmarkCategoryRestaurants(Long userId, Long bookmarkCategoryId);

}