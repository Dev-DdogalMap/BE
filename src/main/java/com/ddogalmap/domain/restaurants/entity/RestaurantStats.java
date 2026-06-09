package com.ddogalmap.domain.restaurants.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 식당별 사전 계산된 통계 (스케줄러로 갱신).
 * 검색/조회 시 reviews/visit_verifications 매번 집계하지 않고
 * 이 테이블 컬럼 값을 그대로 사용.
 *
 * 즐겨찾기 수는 산식에 없고 상세 페이지에서만 표시 → bookmarks 테이블에서 직접 COUNT.
 */
@Getter
@Entity
@Table(name = "restaurant_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantStats {

    @Id
    private Long restaurantId;

    /** 찐맛집지수 (0~100, 소수 첫째자리) */
    @Column(precision = 4, scale = 1)
    private BigDecimal foodScore;

    /** 주민 추천 비율 (0~100) */
    private Integer residentRecommendRate;

    /** 재방문율 (0~100) */
    private Integer revisitRate;

    /** 방문 인증 수 */
    private Long visitVerifyCount;

    /** 평균 별점 (0.0~5.0, 소수 첫째자리) */
    @Column(precision = 2, scale = 1)
    private BigDecimal averageScore;

    /** 리뷰 개수 */
    private Long reviewCount;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public RestaurantStats(
            Long restaurantId,
            BigDecimal foodScore,
            Integer residentRecommendRate,
            Integer revisitRate,
            Long visitVerifyCount,
            BigDecimal averageScore,
            Long reviewCount
    ) {
        this.restaurantId = restaurantId;
        this.foodScore = foodScore;
        this.residentRecommendRate = residentRecommendRate;
        this.revisitRate = revisitRate;
        this.visitVerifyCount = visitVerifyCount;
        this.averageScore = averageScore;
        this.reviewCount = reviewCount;
    }

    /** 통계 갱신 (UPDATE 용). updated_at은 @UpdateTimestamp가 자동 갱신. */
    public void update(
            BigDecimal foodScore,
            Integer residentRecommendRate,
            Integer revisitRate,
            Long visitVerifyCount,
            BigDecimal averageScore,
            Long reviewCount
    ) {
        this.foodScore = foodScore;
        this.residentRecommendRate = residentRecommendRate;
        this.revisitRate = revisitRate;
        this.visitVerifyCount = visitVerifyCount;
        this.averageScore = averageScore;
        this.reviewCount = reviewCount;
    }
}
