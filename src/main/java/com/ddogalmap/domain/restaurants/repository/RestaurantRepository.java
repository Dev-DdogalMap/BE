package com.ddogalmap.domain.restaurants.repository;

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
            @Param("swLat") double swLat, @Param("swLng") double swLng,
            @Param("neLat") double neLat, @Param("neLng") double neLng,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT
        r.restaurant_id AS restaurantId,
        r.place_name AS placeName,
        ft.type AS foodType,
        r.road_address_name AS roadAddressName,
        CAST(
            ST_Distance(
                r.location,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            ) AS INTEGER
        ) AS distance,
        ROUND(AVG(rv.score), 1) AS averageScore,
        COUNT(rv.review_id) AS reviewCount
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN reviews rv
            ON rv.restaurant_id = r.restaurant_id
        WHERE r.restaurant_id = :restaurantId
        GROUP BY r.restaurant_id, r.place_name, ft.type, r.road_address_name, r.location
    """, nativeQuery = true)
    Optional<RestaurantPreviewProjection> findRestaurantPreview(
            @Param("restaurantId") Long restaurantId,@Param("lat") double lat, @Param("lng") double lng);

    /**
     * 맛집 검색 (페이지 단위).
     * - 필터: keyword, region, foodTypeId (각각 null 이면 무시)
     * - 정렬: distance(기본) / jjinScore / score
     *   - distance 1차 → jjinScore 2차
     *   - jjinScore 또는 score 1차 → distance 2차
     * - 산식: 한 사람 점수 = 19 + min(level, 3) * (score - 3)
     *         level NULL 이면 1 로 처리
     *         음식점 jjinScore = AVG(한 사람 점수) * 4
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
                ROUND(AVG(rv.score), 1) AS averageScore,
                COUNT(DISTINCT rv.review_id) AS reviewCount,
                CAST(
                    ROUND(
                        (CASE
                            WHEN COUNT(DISTINCT CASE WHEN r.address_name LIKE CONCAT('%', u.region, '%') THEN rv.review_id END) > 0
                            THEN AVG(CASE WHEN r.address_name LIKE CONCAT('%', u.region, '%') THEN rv.score END) * 20
                            ELSE 0
                        END) * 0.4
                        +
                        0 * 0.3
                        +
                        (CASE
                            WHEN COUNT(DISTINCT rv.review_id) > 0
                            THEN AVG(rv.score) * 20
                            ELSE 0
                        END) * 0.2
                        +
                        (CASE
                            WHEN COUNT(DISTINCT rv.user_id) > 0
                            THEN COUNT(DISTINCT CASE WHEN b.user_id = rv.user_id THEN rv.user_id END) * 100.0 / COUNT(DISTINCT rv.user_id)
                            ELSE 0
                        END) * 0.1
                    ) AS INTEGER
                ) AS jjinScore,
                COUNT(DISTINCT b.bookmark_id) AS bookmarkCount
            FROM restaurants r
            JOIN food_types ft
                ON r.food_type_id = ft.food_type_id
            LEFT JOIN reviews rv
                ON rv.restaurant_id = r.restaurant_id
            LEFT JOIN user_levels ul
                ON ul.user_id = rv.user_id
            LEFT JOIN bookmarks b
                ON b.restaurant_id = r.restaurant_id
            LEFT JOIN users u
                ON u.user_id = rv.user_id
            WHERE 1 = 1
                AND (:keyword IS NULL OR r.place_name LIKE CONCAT('%', :keyword, '%'))
                AND (:region IS NULL OR r.address_name LIKE CONCAT('%', :region, '%'))
                AND (:foodTypeId IS NULL OR r.food_type_id = :foodTypeId)
            GROUP BY
                r.restaurant_id,
                r.place_name,
                ft.type,
                r.address_name,
                r.road_address_name,
                r.location
        ) sub
        ORDER BY
            CASE WHEN :sort = 'distance'  THEN distance     END ASC,
            CASE WHEN :sort = 'jjinScore' THEN jjinScore    END DESC,
            CASE WHEN :sort = 'score'     THEN averageScore END DESC,
            CASE WHEN :sort = 'distance'  THEN jjinScore    END DESC,
            CASE WHEN :sort <> 'distance' THEN distance     END ASC,
            restaurantId DESC
        LIMIT :size OFFSET :offset
    """, nativeQuery = true)
    List<RestaurantSearchProjection> searchRestaurants(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("foodTypeId") Long foodTypeId,
            @Param("lat") double lat,
            @Param("lng") double lng,
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
     * 같은 restaurantId 가 여러 행으로 옴. 자바에서 음식점별 상위 3개 그룹핑.
     * IN 절 1회로 N+1 회피.
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
