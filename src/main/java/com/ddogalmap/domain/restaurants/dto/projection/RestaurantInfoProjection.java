package com.ddogalmap.domain.restaurants.dto.projection;

public interface RestaurantInfoProjection {

	Long getRestaurantId();

	String getPlaceName();

	String getRoadAddressName();

	String getPhone();

	String getPlaceUrl();

	Double getLatitude();

	Double getLongitude();
}