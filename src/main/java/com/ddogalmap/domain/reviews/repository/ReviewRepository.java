package com.ddogalmap.domain.reviews.repository;

import com.ddogalmap.domain.reviews.dto.projection.FoodTypeReviewCountProjection;
import com.ddogalmap.domain.reviews.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    // 특정 식당의 리뷰를 최신슨 둥의 조건에 맞춰 페이징 조회
    Slice<Review> findByRestaurantId(Long restaurantId, Pageable pageable);

    @Query(value = """
        SELECT ri.img_url
        FROM reviews r
        JOIN review_imgs ri
            ON ri.review_id = r.review_id
        JOIN likes l
            ON l.review_id = r.review_id
        WHERE r.restaurant_id = :restaurantId
        GROUP BY r.review_id, ri.img_id, ri.img_url
        HAVING COUNT(l.like_id) >= 10
        ORDER BY COUNT(l.like_id) DESC,
                 r.review_id DESC,
                 ri.img_id ASC
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findRepresentativeImageUrl(@Param("restaurantId") Long restaurantId);

    @Query(value = """
        SELECT t.content
        FROM tags t
        JOIN reviews r
            ON r.review_id = t.review_id
        WHERE r.restaurant_id = :restaurantId
        GROUP BY t.content
        ORDER BY COUNT(*) DESC,
                 t.content ASC
        LIMIT 3
    """, nativeQuery = true)
    List<String> findTop3TagsByRestaurantId(
            @Param("restaurantId") Long restaurantId
    );

    @Query("""
        select count(r)
        from Review r
        where r.user.userId = :userId
    """)
    int countByUserId(@Param("userId") Long userId);

    @Query("""
        select count(r)
        from Review r
        where r.user.userId = :userId
          and r.restaurant.foodType.foodTypeId in (
              select bft.foodType.foodTypeId
              from BadgeFoodType bft
              where bft.badge.badgeId = :badgeId
          )
    """)
    int countByUserIdAndBadgeFoodTypes(
            @Param("userId") Long userId,
            @Param("badgeId") Long badgeId
    );

    @Query("""
        select
            r.restaurant.foodType.foodTypeId as foodTypeId,
            count(r) as reviewCount
        from Review r
        where r.user.userId = :userId
        group by r.restaurant.foodType.foodTypeId
    """)
    List<FoodTypeReviewCountProjection> countReviewsGroupByFoodType(
            @Param("userId") Long userId
    );

    @Query(value = """
        SELECT CONCAT(ft.type, ' 전문')
        FROM reviews r
        JOIN restaurants rt
            ON rt.restaurant_id = r.restaurant_id
        JOIN food_types ft
            ON ft.food_type_id = rt.food_type_id
        WHERE r.user_id = :userId
        GROUP BY ft.type
        ORDER BY COUNT(*) DESC,
                 MAX(r.created_at) DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findTopSpecialtyByUserId(@Param("userId") Long userId);
}
