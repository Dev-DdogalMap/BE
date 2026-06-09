package com.ddogalmap.domain.bookmarks.repository;

import com.ddogalmap.domain.bookmarks.dto.projection.BookmarkRestaurantProjection;
import com.ddogalmap.domain.bookmarks.dto.projection.BookmarkMapRestaurantProjection;
import com.ddogalmap.domain.bookmarks.entity.Bookmark;
import com.ddogalmap.domain.bookmarks.entity.BookmarkCategory;
import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserOrderByCreatedAtDesc(User user);

    List<Bookmark> findAllByUserAndBookmarkCategoryOrderByCreatedAtDesc(
            User user,
            BookmarkCategory bookmarkCategory
    );

    List<Bookmark> findAllByUserAndRestaurant(User user, Restaurant restaurant);

    long countByBookmarkCategory(BookmarkCategory bookmarkCategory);

    boolean existsByUserAndRestaurantAndBookmarkCategory(User user, Restaurant restaurant, BookmarkCategory bookmarkCategory);

    Optional<Bookmark> findByUserAndRestaurantAndBookmarkCategory(User user, Restaurant restaurant, BookmarkCategory bookmarkCategory);

    void deleteAllByBookmarkCategory(BookmarkCategory bookmarkCategory);

    @Query(value = """
    SELECT
        b.bookmark_id AS bookmarkId,
        r.restaurant_id AS restaurantId,
        r.place_name AS restaurantName,
        ft.type AS category,
        COALESCE(r.road_address_name, r.address_name) AS address,
        img.img_url AS imageUrl,
        b.memo AS memo,
        b.created_at AS createdAt,
        ROUND(CAST(COALESCE(rs.avg_score, 0) AS NUMERIC), 1) AS averageScore,
        COALESCE(rs.review_count, 0) AS reviewCount,
        COALESCE(tags.top_tags, '') AS topTags
    FROM bookmarks b
    JOIN restaurants r
        ON r.restaurant_id = b.restaurant_id
    LEFT JOIN food_types ft
        ON ft.food_type_id = r.food_type_id

    LEFT JOIN (
        SELECT
            rv.restaurant_id,
            AVG(rv.score) AS avg_score,
            COUNT(rv.review_id) AS review_count
        FROM reviews rv
        GROUP BY rv.restaurant_id
    ) rs
        ON rs.restaurant_id = r.restaurant_id

    LEFT JOIN (
        SELECT
            tag_rank.restaurant_id,
            STRING_AGG(tag_rank.content, ',' ORDER BY tag_rank.tag_count DESC, tag_rank.content ASC) AS top_tags
        FROM (
            SELECT
                rv.restaurant_id,
                t.content,
                COUNT(*) AS tag_count,
                ROW_NUMBER() OVER (
                    PARTITION BY rv.restaurant_id
                    ORDER BY COUNT(*) DESC, t.content ASC
                ) AS rn
            FROM reviews rv
            JOIN tags t
                ON t.review_id = rv.review_id
            GROUP BY rv.restaurant_id, t.content
        ) tag_rank
        WHERE tag_rank.rn <= 2
        GROUP BY tag_rank.restaurant_id
    ) tags
        ON tags.restaurant_id = r.restaurant_id

    LEFT JOIN LATERAL (
        SELECT
            ri.img_url
        FROM reviews rv_img
        JOIN review_imgs ri
            ON ri.review_id = rv_img.review_id
        WHERE rv_img.restaurant_id = r.restaurant_id
        ORDER BY rv_img.created_at DESC, ri.img_id ASC
        LIMIT 1
    ) img ON true

    WHERE b.user_id = :userId
      AND b.bookmark_category_id = :bookmarkCategoryId
    ORDER BY b.created_at DESC
""", nativeQuery = true)
    List<BookmarkRestaurantProjection> findBookmarkRestaurantsByCategory(
            @Param("userId") Long userId,
            @Param("bookmarkCategoryId") Long bookmarkCategoryId
    );

    @Query(value = """
            SELECT
                b.bookmark_id AS bookmarkId,
                r.restaurant_id AS restaurantId,
                r.place_name AS placeName,
                ft.type AS foodType,
                r.address_name AS addressName,
                ST_Y(r.location::geometry) AS latitude,
                ST_X(r.location::geometry) AS longitude
            FROM bookmarks b
            JOIN restaurants r
                ON b.restaurant_id = r.restaurant_id
            JOIN food_types ft
                ON r.food_type_id = ft.food_type_id
            WHERE b.user_id = :userId
              AND b.bookmark_category_id = :categoryId
            ORDER BY b.created_at DESC
            """, nativeQuery = true)
    List<BookmarkMapRestaurantProjection> findBookmarkMapRestaurants(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId
    );
}
