package com.ddogalmap.domain.restaurants.dto.projection;

public interface RestaurantInfoProjection {

	Long getRestaurantId();
	String getPlaceName();
	String getRoadAddressName();
	String getPhone();
	Double getLatitude();
	Double getLongitude();
	String getFoodType();
	Integer getDistance();
	Double getAverageScore();
	Long getReviewCount();
}