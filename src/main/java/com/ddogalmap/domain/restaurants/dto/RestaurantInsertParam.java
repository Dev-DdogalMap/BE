package com.ddogalmap.domain.restaurants.dto;

/**
 * 한 행의 INSERT 파라미터.
 * 좌표 x, y는 EPSG:2097 (서울 공공데이터) 기준.
 * Repository에서 ST_Transform 으로 4326으로 변환.
 */
public record RestaurantInsertParam(
        String managementNo,
        String placeName,
        Long foodTypeId,
        String phone,
        String addressName,
        String roadAddressName,
        Double x,
        Double y,
        String placeUrl
) {
}
