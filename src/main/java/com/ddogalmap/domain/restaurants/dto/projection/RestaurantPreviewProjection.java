package com.ddogalmap.domain.restaurants.dto.projection;

public interface RestaurantPreviewProjection {
    Long getRestaurantId();
    String getPlaceName();
    String getFoodType();
    String getRoadAddressName();
    Integer getDistance();
    Double getAverageScore();
    Long getReviewCount();
}
