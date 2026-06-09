package com.ddogalmap.domain.bookmarks.dto.projection;

public interface BookmarkMapRestaurantProjection {

	Long getBookmarkId();

	Long getRestaurantId();

	String getPlaceName();

	String getFoodType();

	String getAddressName();

	Double getLatitude();

	Double getLongitude();
}