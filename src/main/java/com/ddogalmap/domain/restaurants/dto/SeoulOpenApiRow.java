package com.ddogalmap.domain.restaurants.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 서울 열린데이터광장 OA-16094 응답의 단일 row.
 * 응답 필드는 모두 대문자이고 String으로 옴.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SeoulOpenApiRow(
        @JsonProperty("MGTNO") String managementNo,
        @JsonProperty("BPLCNM") String placeName,
        @JsonProperty("UPTAENM") String foodType,
        @JsonProperty("TRDSTATEGBN") String stateCode,
        @JsonProperty("TRDSTATENM") String stateName,
        @JsonProperty("SITETEL") String phone,
        @JsonProperty("SITEWHLADDR") String addressName,
        @JsonProperty("RDNWHLADDR") String roadAddressName,
        @JsonProperty("X") String x,
        @JsonProperty("Y") String y,
        @JsonProperty("HOMEPAGE") String homepage
) {
}
