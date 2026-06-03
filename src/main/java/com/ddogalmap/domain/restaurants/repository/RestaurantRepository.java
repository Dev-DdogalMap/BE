package com.ddogalmap.domain.restaurants.repository;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantInfoProjection;
import com.ddogalmap.domain.restaurants.dto.projection.RestaurantMapProjection;
import com.ddogalmap.domain.restaurants.dto.projection.RestaurantPreviewProjection;
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
            @Param("restaurantId") Long restaurantId, @Param("lat") Double lat, @Param("lng") Double lng);


    @Query(value = """
    SELECT
        r.restaurant_id AS restaurantId,
        r.place_name AS placeName,
        r.road_address_name AS roadAddressName,
        r.phone AS phone,
        r.place_url AS placeUrl,
        ST_Y(r.location::geometry) AS latitude,
        ST_X(r.location::geometry) AS longitude
    FROM restaurants r
    WHERE r.restaurant_id = :restaurantId
    """,
            nativeQuery = true)
    Optional<RestaurantInfoProjection> findRestaurantInfo(
            @Param("restaurantId") Long restaurantId
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

}
