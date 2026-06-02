package com.ddogalmap.domain.restaurants.dto.projection;

/**
 * 맛집 검색 메인 쿼리 결과 한 행.
 */
public interface RestaurantSearchProjection {
    Long getRestaurantId();
    String getPlaceName();
    String getFoodType();
    String getAddressName();
    String getRoadAddressName();
    Double getLatitude();
    Double getLongitude();
    Integer getDistance();
    Double getAverageScore();
    Long getReviewCount();
    Integer getJjinScore();
    Long getBookmarkCount();
}
