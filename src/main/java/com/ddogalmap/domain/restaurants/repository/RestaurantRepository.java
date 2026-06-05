package com.ddogalmap.domain.restaurants.repository;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantInfoProjection;
import com.ddogalmap.domain.restaurants.dto.projection.RestaurantMapProjection;
import com.ddogalmap.domain.restaurants.dto.projection.RestaurantPreviewProjection;
import com.ddogalmap.domain.restaurants.dto.projection.RestaurantSearchProjection;
import com.ddogalmap.domain.restaurants.dto.projection.RestaurantTagProjection;
import com.ddogalmap.domain.restaurants.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByManagementNo(String managementNo);

    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            r.place_name AS placeName,
            ft.food_type_id AS foodTypeId,
            ft.type AS foodType,
            r.address_name AS addressName,
            ST_Y(r.location::geometry) AS latitude,
            ST_X(r.location::geometry) AS longitude
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        WHERE r.location::geometry && ST_MakeEnvelope(
            :swLng, :swLat,
            :neLng, :neLat,
            4326
        )
        ORDER BY r.restaurant_id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<RestaurantMapProjection> findRestaurantsInBounds(
            @Param("swLat") double swLat,
            @Param("swLng") double swLng,
            @Param("neLat") double neLat,
            @Param("neLng") double neLng,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            r.place_name AS placeName,
            ft.type AS foodType,
            r.road_address_name AS roadAddressName,
            CASE
                WHEN :lat IS NULL OR :lng IS NULL THEN NULL
                ELSE CAST(
                    ST_Distance(
                        r.location,
                        ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
                    ) AS INTEGER
                )
            END AS distance,
            ROUND(rs.avg_score, 1) AS averageScore,
            COALESCE(rs.review_count, 0) AS reviewCount,

            -- 전체 맛집지수 (4-factor 가중합, 소수 첫째자리)
            ROUND(
                CAST(
                    COALESCE(rs.resident_avg_score, 0) * 20 * 0.4
                    +
                    (CASE
                        WHEN COALESCE(rs.review_count, 0) > 0
                        THEN COALESCE(rs.revisit_count, 0) * 100.0 / rs.review_count
                        ELSE 0
                    END) * 0.3
                    +
                    COALESCE(rs.avg_score, 0) * 20 * 0.2
                    +
                    (CASE
                        WHEN COALESCE(rs.distinct_user_count, 0) > 0
                        THEN COALESCE(rs.bookmarked_reviewer_count, 0) * 100.0 / rs.distinct_user_count
                        ELSE 0
                    END) * 0.1
                AS NUMERIC)
            , 1) AS foodScore
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN (
            SELECT
                rv.restaurant_id,
                COUNT(DISTINCT rv.review_id) AS review_count,
                AVG(rv.score) AS avg_score,
                COUNT(DISTINCT CASE WHEN rv.is_revisit = TRUE THEN rv.review_id END) AS revisit_count,
                COUNT(DISTINCT rv.user_id) AS distinct_user_count,
                COUNT(DISTINCT CASE WHEN rb.user_id IS NOT NULL THEN rv.user_id END) AS bookmarked_reviewer_count,
                AVG(CASE WHEN ru.region IS NOT NULL AND rr.address_name LIKE CONCAT('%', ru.region, '%') THEN rv.score END) AS resident_avg_score
            FROM reviews rv
            JOIN restaurants rr ON rr.restaurant_id = rv.restaurant_id
            LEFT JOIN users ru ON ru.user_id = rv.user_id
            LEFT JOIN bookmarks rb ON rb.restaurant_id = rv.restaurant_id AND rb.user_id = rv.user_id
            WHERE rv.restaurant_id = :restaurantId
            GROUP BY rv.restaurant_id
        ) rs ON rs.restaurant_id = r.restaurant_id
        WHERE r.restaurant_id = :restaurantId
    """, nativeQuery = true)
    Optional<RestaurantPreviewProjection> findRestaurantPreview(
            @Param("restaurantId") Long restaurantId,
            @Param("lat") Double lat,
            @Param("lng") Double lng
    );



    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            r.place_name AS placeName,
            ft.type AS foodType,
            r.road_address_name AS roadAddressName,
            r.phone AS phone,
            r.place_url AS placeUrl,
            ST_Y(r.location::geometry) AS latitude,
            ST_X(r.location::geometry) AS longitude,
            CASE
                WHEN :lat IS NULL OR :lng IS NULL THEN NULL
                ELSE CAST(
                    ST_Distance(
                        r.location,
                        ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
                    ) AS INTEGER
                )
            END AS distance,
            ROUND(rs.avg_score, 1) AS averageScore,
            COALESCE(rs.review_count, 0) AS reviewCount,

            -- 주민 추천 비율 (0~100, 별점 100점 환산)
            CAST(
                CASE
                    WHEN rs.resident_avg_score IS NOT NULL
                    THEN ROUND(rs.resident_avg_score * 20)
                    ELSE 0
                END AS INTEGER
            ) AS residentRecommendRate,

            -- 재방문율 (is_revisit=TRUE 비율, 0~100)
            CAST(
                CASE
                    WHEN COALESCE(rs.review_count, 0) > 0
                    THEN ROUND(COALESCE(rs.revisit_count, 0) * 100.0 / rs.review_count)
                    ELSE 0
                END AS INTEGER
            ) AS revisitRate,

            -- 방문 인증 수
            COALESCE(vs.visit_verify_count, 0) AS visitVerifyCount,

            -- 즐겨찾기 수
            COALESCE(bs.bookmark_count, 0) AS bookmarkCount,

            -- 전체 맛집지수 (4-factor 가중합, 소수 첫째자리)
            ROUND(
                CAST(
                    COALESCE(rs.resident_avg_score, 0) * 20 * 0.4
                    +
                    (CASE
                        WHEN COALESCE(rs.review_count, 0) > 0
                        THEN COALESCE(rs.revisit_count, 0) * 100.0 / rs.review_count
                        ELSE 0
                    END) * 0.3
                    +
                    COALESCE(rs.avg_score, 0) * 20 * 0.2
                    +
                    (CASE
                        WHEN COALESCE(rs.distinct_user_count, 0) > 0
                        THEN COALESCE(rs.bookmarked_reviewer_count, 0) * 100.0 / rs.distinct_user_count
                        ELSE 0
                    END) * 0.1
                AS NUMERIC)
            , 1) AS foodScore
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN (
            SELECT
                rv.restaurant_id,
                COUNT(DISTINCT rv.review_id) AS review_count,
                AVG(rv.score) AS avg_score,
                COUNT(DISTINCT CASE WHEN rv.is_revisit = TRUE THEN rv.review_id END) AS revisit_count,
                COUNT(DISTINCT rv.user_id) AS distinct_user_count,
                COUNT(DISTINCT CASE WHEN rb.user_id IS NOT NULL THEN rv.user_id END) AS bookmarked_reviewer_count,
                AVG(CASE WHEN ru.region IS NOT NULL AND rr.address_name LIKE CONCAT('%', ru.region, '%') THEN rv.score END) AS resident_avg_score
            FROM reviews rv
            JOIN restaurants rr ON rr.restaurant_id = rv.restaurant_id
            LEFT JOIN users ru ON ru.user_id = rv.user_id
            LEFT JOIN bookmarks rb ON rb.restaurant_id = rv.restaurant_id AND rb.user_id = rv.user_id
            WHERE rv.restaurant_id = :restaurantId
            GROUP BY rv.restaurant_id
        ) rs ON rs.restaurant_id = r.restaurant_id
        LEFT JOIN (
            SELECT b.restaurant_id, COUNT(*) AS bookmark_count
            FROM bookmarks b
            WHERE b.restaurant_id = :restaurantId
            GROUP BY b.restaurant_id
        ) bs ON bs.restaurant_id = r.restaurant_id
        LEFT JOIN (
            SELECT vv.restaurant_id, COUNT(*) AS visit_verify_count
            FROM visit_verifications vv
            WHERE vv.restaurant_id = :restaurantId
            GROUP BY vv.restaurant_id
        ) vs ON vs.restaurant_id = r.restaurant_id
        WHERE r.restaurant_id = :restaurantId
    """, nativeQuery = true)
    Optional<RestaurantInfoProjection> findRestaurantInfo(
                @Param("restaurantId") Long restaurantId,
                @Param("lat") Double lat,
                @Param("lng") Double lng
        );

    @Query(value = """
        SELECT
            ST_Y(r.location::geometry)
        FROM restaurants r
        WHERE r.restaurant_id = :restaurantId
    """, nativeQuery = true)
    Double findLatitudeByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query(value = """
        SELECT
            ST_X(r.location::geometry)
        FROM restaurants r
        WHERE r.restaurant_id = :restaurantId
    """, nativeQuery = true)
    Double findLongitudeByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query(value = """
        SELECT
            ST_Distance(
                r.location,
                ST_SetSRID(ST_MakePoint(:userLongitude, :userLatitude), 4326)::geography
            )
        FROM restaurants r
        WHERE r.restaurant_id = :restaurantId
    """, nativeQuery = true)
    Double calculateDistanceMeter(
            @Param("restaurantId") Long restaurantId,
            @Param("userLatitude") Double userLatitude,
            @Param("userLongitude") Double userLongitude
    );

    /**
     * 맛집 검색 (페이지 단위).
     * - 필터: keyword, region, foodTypeId (각각 null 이면 무시)
     * - 정렬: distance(기본) / jjinScore / score
     */
    @Query(value = """
        SELECT * FROM (
            SELECT
                r.restaurant_id AS restaurantId,
                r.place_name AS placeName,
                ft.type AS foodType,
                r.address_name AS addressName,
                r.road_address_name AS roadAddressName,
                ST_Y(r.location::geometry) AS latitude,
                ST_X(r.location::geometry) AS longitude,
                CAST(
                    ST_Distance(
                        r.location,
                        ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
                    ) AS INTEGER
                ) AS distance,
                ROUND(rs.avg_score, 1) AS averageScore,
                COALESCE(rs.review_count, 0) AS reviewCount,
                CAST(
                    ROUND(
                        COALESCE(rs.resident_avg_score, 0) * 20 * 0.4
                        +
                        (CASE
                            WHEN COALESCE(rs.review_count, 0) > 0
                            THEN COALESCE(rs.revisit_count, 0) * 100.0 / rs.review_count
                            ELSE 0
                        END) * 0.3
                        +
                        COALESCE(rs.avg_score, 0) * 20 * 0.2
                        +
                        (CASE
                            WHEN COALESCE(rs.distinct_user_count, 0) > 0
                            THEN COALESCE(rs.bookmarked_reviewer_count, 0) * 100.0 / rs.distinct_user_count
                            ELSE 0
                        END) * 0.1
                    ) AS INTEGER
                ) AS jjinScore,
                COALESCE(bs.bookmark_count, 0) AS bookmarkCount
            FROM restaurants r
            JOIN food_types ft
                ON r.food_type_id = ft.food_type_id
            LEFT JOIN (
                SELECT
                    rv.restaurant_id,
                    COUNT(DISTINCT rv.review_id) AS review_count,
                    AVG(rv.score) AS avg_score,
                    COUNT(DISTINCT CASE WHEN rv.is_revisit = TRUE THEN rv.review_id END) AS revisit_count,
                    COUNT(DISTINCT rv.user_id) AS distinct_user_count,
                    COUNT(DISTINCT CASE WHEN rb.user_id IS NOT NULL THEN rv.user_id END) AS bookmarked_reviewer_count,
                    AVG(CASE WHEN ru.region IS NOT NULL AND rr.address_name LIKE CONCAT('%', ru.region, '%') THEN rv.score END) AS resident_avg_score
                FROM reviews rv
                JOIN restaurants rr ON rr.restaurant_id = rv.restaurant_id
                LEFT JOIN users ru ON ru.user_id = rv.user_id
                LEFT JOIN bookmarks rb ON rb.restaurant_id = rv.restaurant_id AND rb.user_id = rv.user_id
                GROUP BY rv.restaurant_id
            ) rs ON rs.restaurant_id = r.restaurant_id
            LEFT JOIN (
                SELECT b.restaurant_id, COUNT(*) AS bookmark_count
                FROM bookmarks b
                GROUP BY b.restaurant_id
            ) bs ON bs.restaurant_id = r.restaurant_id
            WHERE 1 = 1
                AND (:keyword IS NULL OR r.place_name LIKE CONCAT('%', :keyword, '%'))
                AND (:region IS NULL OR r.address_name LIKE CONCAT('%', :region, '%'))
                AND (:foodTypeId IS NULL OR r.food_type_id = :foodTypeId)
        ) sub
        ORDER BY
            CASE WHEN :sort = 'distance'  THEN distance     END ASC,
            CASE WHEN :sort = 'jjinScore' THEN jjinScore    END DESC,
            CASE WHEN :sort = 'score'     THEN averageScore END DESC NULLS LAST,
            CASE WHEN :sort = 'distance'  THEN jjinScore    END DESC,
            CASE WHEN :sort <> 'distance' THEN distance     END ASC,
            restaurantId DESC
        LIMIT :size OFFSET :offset
    """, nativeQuery = true)
    List<RestaurantSearchProjection> searchRestaurants(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("foodTypeId") Long foodTypeId,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("sort") String sort,
            @Param("size") int size,
            @Param("offset") int offset
    );

    /**
     * 맛집 검색 결과 총 건수 (페이징 메타용).
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM restaurants r
        WHERE 1 = 1
            AND (:keyword IS NULL OR r.place_name LIKE CONCAT('%', :keyword, '%'))
            AND (:region IS NULL OR r.address_name LIKE CONCAT('%', :region, '%'))
            AND (:foodTypeId IS NULL OR r.food_type_id = :foodTypeId)
    """, nativeQuery = true)
    long countSearchRestaurants(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("foodTypeId") Long foodTypeId
    );

    /**
     * 음식점 ID 리스트로 태그 카운트 (음식점별).
     */
    @Query(value = """
        SELECT
            rv.restaurant_id AS restaurantId,
            t.content AS tag,
            COUNT(*) AS tagCount
        FROM tags t
        JOIN reviews rv
            ON t.review_id = rv.review_id
        WHERE rv.restaurant_id IN (:restaurantIds)
        GROUP BY rv.restaurant_id, t.content
        ORDER BY rv.restaurant_id, tagCount DESC
    """, nativeQuery = true)
    List<RestaurantTagProjection> findTagsByRestaurantIds(
            @Param("restaurantIds") List<Long> restaurantIds
    );
}