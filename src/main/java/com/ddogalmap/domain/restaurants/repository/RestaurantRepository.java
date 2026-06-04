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
            ROUND(AVG(rv.score), 1) AS averageScore,
            COUNT(rv.review_id) AS reviewCount
        FROM restaurants r
        JOIN food_types ft
            ON r.food_type_id = ft.food_type_id
        LEFT JOIN reviews rv
            ON rv.restaurant_id = r.restaurant_id
        WHERE r.restaurant_id = :restaurantId
        GROUP BY
            r.restaurant_id,
            r.place_name,
            ft.type,
            r.road_address_name,
            r.phone,
            r.place_url,
            r.location
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