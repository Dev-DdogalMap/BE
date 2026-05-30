package com.ddogalmap.domain.restaurants.dto.projection;

/**
 * 지도 내 식당 마커 조회 결과
 */
public interface RestaurantMapProjection {

    Long getRestaurantId();
    String getPlaceName();
    Long getFoodTypeId();
    String getFoodType();
    String getAddressName();
    Double getLongitude();
    Double getLatitude();
}
