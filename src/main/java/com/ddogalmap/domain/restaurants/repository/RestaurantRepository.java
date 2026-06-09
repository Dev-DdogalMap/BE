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
            rs.average_score AS averageScore,
            COALESCE(rs.review_count, 0) AS reviewCount,
            rs.food_score AS foodScore
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN restaurant_stats rs
            ON rs.restaurant_id = r.restaurant_id
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
            rs.average_score AS averageScore,
            COALESCE(rs.review_count, 0) AS reviewCount,
            COALESCE(rs.resident_recommend_rate, 0) AS residentRecommendRate,
            COALESCE(rs.revisit_rate, 0) AS revisitRate,
            COALESCE(rs.visit_verify_count, 0) AS visitVerifyCount,
            COALESCE(bs.bookmark_count, 0) AS bookmarkCount,
            rs.food_score AS foodScore
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN restaurant_stats rs
            ON rs.restaurant_id = r.restaurant_id
        LEFT JOIN (
            SELECT b.restaurant_id, COUNT(*) AS bookmark_count
            FROM bookmarks b
            WHERE b.restaurant_id = :restaurantId
            GROUP BY b.restaurant_id
        ) bs ON bs.restaurant_id = r.restaurant_id
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
     * - 점수/카운트는 사전 계산된 restaurant_stats 에서 가져옴 (실시간 집계 X)
     */
    /**
     * 거리순 정렬 (location <-> point KNN 인덱스 활용 가능).
     */
    @Query(value = """
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
            rs.average_score AS averageScore,
            COALESCE(rs.review_count, 0) AS reviewCount,
            CAST(COALESCE(rs.food_score, 0) AS INTEGER) AS jjinScore
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN restaurant_stats rs
            ON rs.restaurant_id = r.restaurant_id
        WHERE 1 = 1
            AND (:keyword IS NULL OR r.place_name LIKE CONCAT('%', :keyword, '%'))
            AND (:region IS NULL OR r.address_name LIKE CONCAT('%', :region, '%'))
            AND (:foodTypeId IS NULL OR r.food_type_id = :foodTypeId)
        ORDER BY
            r.location <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography ASC,
            r.restaurant_id DESC
        LIMIT :size OFFSET :offset
    """, nativeQuery = true)
    List<RestaurantSearchProjection> searchRestaurantsByDistance(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("foodTypeId") Long foodTypeId,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("size") int size,
            @Param("offset") int offset
    );

    /**
     * 맛집지수(jjinScore)순 정렬 (restaurant_stats.food_score 인덱스 활용).
     */
    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            r.place_name AS placeName,
            ft.type AS foodType,
            r.address_name AS addressName,
            r.road_address_name AS roadAddressName,
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
            rs.average_score AS averageScore,
            COALESCE(rs.review_count, 0) AS reviewCount,
            CAST(COALESCE(rs.food_score, 0) AS INTEGER) AS jjinScore
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN restaurant_stats rs
            ON rs.restaurant_id = r.restaurant_id
        WHERE 1 = 1
            AND (:keyword IS NULL OR r.place_name LIKE CONCAT('%', :keyword, '%'))
            AND (:region IS NULL OR r.address_name LIKE CONCAT('%', :region, '%'))
            AND (:foodTypeId IS NULL OR r.food_type_id = :foodTypeId)
        ORDER BY
            rs.food_score DESC NULLS LAST,
            r.restaurant_id DESC
        LIMIT :size OFFSET :offset
    """, nativeQuery = true)
    List<RestaurantSearchProjection> searchRestaurantsByJjinScore(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("foodTypeId") Long foodTypeId,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("size") int size,
            @Param("offset") int offset
    );

    /**
     * 별점순 정렬 (restaurant_stats.average_score 인덱스 활용).
     */
    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            r.place_name AS placeName,
            ft.type AS foodType,
            r.address_name AS addressName,
            r.road_address_name AS roadAddressName,
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
            rs.average_score AS averageScore,
            COALESCE(rs.review_count, 0) AS reviewCount,
            CAST(COALESCE(rs.food_score, 0) AS INTEGER) AS jjinScore
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN restaurant_stats rs
            ON rs.restaurant_id = r.restaurant_id
        WHERE 1 = 1
            AND (:keyword IS NULL OR r.place_name LIKE CONCAT('%', :keyword, '%'))
            AND (:region IS NULL OR r.address_name LIKE CONCAT('%', :region, '%'))
            AND (:foodTypeId IS NULL OR r.food_type_id = :foodTypeId)
        ORDER BY
            rs.average_score DESC NULLS LAST,
            r.restaurant_id DESC
        LIMIT :size OFFSET :offset
    """, nativeQuery = true)
    List<RestaurantSearchProjection> searchRestaurantsByScore(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("foodTypeId") Long foodTypeId,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
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