package com.ddogalmap.domain.restaurants.dto.projection;

/**
 * 음식점별 태그 카운트 결과 한 행.
 * 같은 restaurantId 가 여러 행으로 옴 (tag 종류만큼).
 */
public interface RestaurantTagProjection {
    Long getRestaurantId();
    String getTag();
    Long getTagCount();
}
