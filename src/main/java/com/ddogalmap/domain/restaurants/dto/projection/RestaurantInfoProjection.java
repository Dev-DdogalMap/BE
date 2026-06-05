package com.ddogalmap.domain.restaurants.dto.projection;

public interface RestaurantInfoProjection {

	Long getRestaurantId();
	String getPlaceName();
	String getRoadAddressName();
	String getPhone();
	String getPlaceUrl();
	Double getLatitude();
	Double getLongitude();
	String getFoodType();
	Integer getDistance();
	Double getAverageScore();
	Long getReviewCount();
	Double getFoodScore();               // 전체 맛집지수 (0~100, 소수 첫째자리)
	Integer getResidentRecommendRate();  // 주민 추천 비율 (0~100)
	Integer getRevisitRate();            // 재방문율 (0~100, is_revisit=TRUE 비율)
	Long getVisitVerifyCount();          // 방문 인증 수
	Long getBookmarkCount();             // 즐겨찾기 수
}