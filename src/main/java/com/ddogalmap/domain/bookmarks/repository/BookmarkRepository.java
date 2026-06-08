package com.ddogalmap.domain.bookmarks.repository;

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
