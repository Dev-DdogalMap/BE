package com.ddogalmap.domain.reviews.dto.projection;

/**
 * 음식점별 대표 후기 이미지 (검색 결과 카드용).
 * - 좋아요 최다 후기의 이미지 1장
 * - 후기 없거나 이미지 없는 음식점은 row 자체가 안 나옴 → 매핑 단계에서 null 처리
 */
public interface RestaurantRepresentativeImageProjection {
    Long getRestaurantId();
    String getImgUrl();
}
