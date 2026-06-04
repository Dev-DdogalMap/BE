package com.ddogalmap.domain.restaurants.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 행정안전부 식품_일반음식점 조회서비스 응답의 단일 item.
 * Endpoint: https://apis.data.go.kr/1741000/general_restaurants/info
 *
 * 좌표계: EPSG:5174 (Bessel 중부원점 TM, 보정계수 없음)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneralRestaurantsItem(
        @JsonProperty("MNG_NO") String managementNo,
        @JsonProperty("BPLC_NM") String placeName,
        @JsonProperty("BZSTAT_SE_NM") String foodType,
        @JsonProperty("SALS_STTS_CD") String stateCode,
        @JsonProperty("SALS_STTS_NM") String stateName,
        @JsonProperty("TELNO") String phone,
        @JsonProperty("LOTNO_ADDR") String addressName,
        @JsonProperty("ROAD_NM_ADDR") String roadAddressName,
        @JsonProperty("CRD_INFO_X") String x,
        @JsonProperty("CRD_INFO_Y") String y,
        @JsonProperty("HPG") String homepage
) {
}
