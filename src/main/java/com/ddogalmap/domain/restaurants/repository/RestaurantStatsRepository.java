package com.ddogalmap.domain.restaurants.repository;

import com.ddogalmap.domain.restaurants.entity.RestaurantStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RestaurantStatsRepository extends JpaRepository<RestaurantStats, Long> {

    /**
     * 마지막 배치 시각 = 가장 최근 updated_at.
     * 비어있으면 (테이블 처음 채울 때) 빈 Optional.
     */
    @Query("SELECT MAX(s.updatedAt) FROM RestaurantStats s")
    Optional<LocalDateTime> findLastBatchTime();

    /**
     * 마지막 배치 시각 이후 산식에 영향이 있었던 식당 ID 추출.
     * 합집합 (DISTINCT):
     * - reviews.created_at > :since                 (새 리뷰)
     * - visit_verifications.verified_at > :since    (새 방문 인증)
     * - users.region_verified_at > :since           (reviewer 의 지역 인증 변경 → 주민 추천 평점에 영향)
     * - user_levels.updated_at > :since             (reviewer 의 레벨 변경 → 레벨 가중 평균에 영향)
     *
     * (북마크는 산식에 영향 주지 않으므로 트리거에서 제외)
     */
    @Query(value = """
        SELECT DISTINCT restaurant_id FROM (
            SELECT restaurant_id FROM reviews
                WHERE created_at > :since
            UNION
            SELECT restaurant_id FROM visit_verifications
                WHERE verified_at > :since
            UNION
            SELECT DISTINCT rv.restaurant_id
                FROM reviews rv
                JOIN users u ON u.user_id = rv.user_id
                WHERE u.region_verified_at > :since
            UNION
            SELECT DISTINCT rv.restaurant_id
                FROM reviews rv
                JOIN user_levels ul ON ul.user_id = rv.user_id
                WHERE ul.updated_at > :since
        ) AS changed
    """, nativeQuery = true)
    List<Long> findChangedRestaurantIdsSince(@Param("since") LocalDateTime since);

    /**
     * 전체 식당 ID (최초 배치 시 사용).
     */
    @Query(value = "SELECT restaurant_id FROM restaurants", nativeQuery = true)
    List<Long> findAllRestaurantIds();

    /**
     * 주어진 식당 ID 리스트에 대해 통계를 계산하여 UPSERT.
     * - 기존 row 있으면 UPDATE, 없으면 INSERT (ON CONFLICT)
     * - 한 번의 SQL로 모든 집계 + 가중합 + 저장 처리
     * - <b>review_count > 0 인 식당만 row 생성/갱신</b>.
     *   리뷰가 0 인 식당은 stats 테이블에 row 가 만들어지지 않고,
     *   조회 시 LEFT JOIN 결과 NULL 로 처리됨 (화면에서 "계산중" 표시).
     */
    @Modifying
    @Query(value = """
        INSERT INTO restaurant_stats (
            restaurant_id, food_score, resident_recommend_rate, revisit_rate,
            visit_verify_count, average_score, review_count, updated_at
        )
        SELECT
            r.restaurant_id,
            ROUND(CAST(
                COALESCE(rs.resident_avg_score, 0) * 20 * 0.4
                + (CASE
                       WHEN COALESCE(rs.review_count, 0) > 0
                       THEN COALESCE(rs.revisit_count, 0) * 100.0 / rs.review_count
                       ELSE 0
                   END) * 0.35
                + COALESCE(rs.non_resident_avg_score, 0) * 20 * 0.25
                AS NUMERIC
            ), 1)::NUMERIC(4,1) AS food_score,
            CAST(CASE
                WHEN rs.resident_avg_score IS NOT NULL
                THEN ROUND(rs.resident_avg_score * 20)
                ELSE 0
            END AS INTEGER) AS resident_recommend_rate,
            CAST(CASE
                WHEN COALESCE(rs.review_count, 0) > 0
                THEN ROUND(COALESCE(rs.revisit_count, 0) * 100.0 / rs.review_count)
                ELSE 0
            END AS INTEGER) AS revisit_rate,
            COALESCE(vs.visit_verify_count, 0) AS visit_verify_count,
            ROUND(rs.avg_score, 1)::NUMERIC(2,1) AS average_score,
            COALESCE(rs.review_count, 0) AS review_count,
            CURRENT_TIMESTAMP AS updated_at
        FROM restaurants r
        LEFT JOIN (
            SELECT
                rv.restaurant_id,
                COUNT(DISTINCT rv.review_id) AS review_count,
                AVG(rv.score) AS avg_score,
                COUNT(DISTINCT CASE WHEN rv.is_revisit = TRUE THEN rv.review_id END) AS revisit_count,
                -- 주민 추천 평점 (레벨 가중 평균)
                SUM(CASE
                        WHEN ru.region IS NOT NULL
                         AND rr.address_name LIKE CONCAT('%', ru.region, '%')
                        THEN rv.score * COALESCE(l.level, 1) ELSE 0
                    END)::NUMERIC
                / NULLIF(SUM(CASE
                        WHEN ru.region IS NOT NULL
                         AND rr.address_name LIKE CONCAT('%', ru.region, '%')
                        THEN COALESCE(l.level, 1) ELSE 0
                    END), 0) AS resident_avg_score,
                -- 주민 제외 평점 (레벨 가중 평균)
                SUM(CASE
                        WHEN ru.region IS NULL
                         OR rr.address_name NOT LIKE CONCAT('%', ru.region, '%')
                        THEN rv.score * COALESCE(l.level, 1) ELSE 0
                    END)::NUMERIC
                / NULLIF(SUM(CASE
                        WHEN ru.region IS NULL
                         OR rr.address_name NOT LIKE CONCAT('%', ru.region, '%')
                        THEN COALESCE(l.level, 1) ELSE 0
                    END), 0) AS non_resident_avg_score
            FROM reviews rv
            JOIN restaurants rr ON rr.restaurant_id = rv.restaurant_id
            LEFT JOIN users ru ON ru.user_id = rv.user_id
            LEFT JOIN (
                -- 사용자당 가장 최신 user_levels row 1개만 (updated_at DESC)
                SELECT DISTINCT ON (user_id) user_id, level_id
                FROM user_levels
                ORDER BY user_id, updated_at DESC
            ) ul ON ul.user_id = rv.user_id
            LEFT JOIN levels l ON l.level_id = ul.level_id
            WHERE rv.restaurant_id IN (:restaurantIds)
            GROUP BY rv.restaurant_id
        ) rs ON rs.restaurant_id = r.restaurant_id
        LEFT JOIN (
            SELECT vv.restaurant_id, COUNT(*) AS visit_verify_count
            FROM visit_verifications vv
            WHERE vv.restaurant_id IN (:restaurantIds)
            GROUP BY vv.restaurant_id
        ) vs ON vs.restaurant_id = r.restaurant_id
        WHERE r.restaurant_id IN (:restaurantIds)
          AND COALESCE(rs.review_count, 0) > 0  -- 후기 있는 식당만 row 추가
        ON CONFLICT (restaurant_id) DO UPDATE SET
            food_score = EXCLUDED.food_score,
            resident_recommend_rate = EXCLUDED.resident_recommend_rate,
            revisit_rate = EXCLUDED.revisit_rate,
            visit_verify_count = EXCLUDED.visit_verify_count,
            average_score = EXCLUDED.average_score,
            review_count = EXCLUDED.review_count,
            updated_at = CURRENT_TIMESTAMP
    """, nativeQuery = true)
    int upsertStatsForRestaurantIds(@Param("restaurantIds") List<Long> restaurantIds);
}
