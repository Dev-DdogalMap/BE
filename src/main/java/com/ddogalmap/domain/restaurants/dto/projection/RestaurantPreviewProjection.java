package com.ddogalmap.domain.restaurants.dto.projection;

public interface RestaurantPreviewProjection {
    Long getRestaurantId();
    String getPlaceName();
    String getFoodType();
    String getRoadAddressName();
    Integer getDistance();
    Double getAverageScore();
    Long getReviewCount();
    Double getFoodScore();               // 전체 맛집지수 (0~100, 소수 첫째자리)
}
