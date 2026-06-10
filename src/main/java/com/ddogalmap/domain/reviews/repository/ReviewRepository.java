package com.ddogalmap.domain.reviews.repository;

import com.ddogalmap.domain.reviews.dto.projection.FoodTypeReviewCountProjection;
import com.ddogalmap.domain.reviews.dto.projection.RestaurantRepresentativeImageProjection;
import com.ddogalmap.domain.reviews.entity.Review;
import com.ddogalmap.domain.visit.entity.VisitVerification;
import org.springframework.data.domain.Page;
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
    Page<Review> findByUserId(Long userId, Pageable pageable);

    boolean existsByVisitVerification(VisitVerification visitVerification);

    @Query(value = """
        SELECT ri.img_url
        FROM reviews r
        JOIN review_imgs ri
            ON ri.review_id = r.review_id
        LEFT JOIN likes l
            ON l.review_id = r.review_id
        WHERE r.restaurant_id = :restaurantId
        GROUP BY r.review_id, ri.img_id, ri.img_url
        ORDER BY COUNT(l.like_id) DESC,
                 r.review_id DESC,
                 ri.img_id ASC
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findRepresentativeImageUrl(@Param("restaurantId") Long restaurantId);

    /**
     * 검색 결과 카드용 — 음식점 ID 리스트에 대해 대표 이미지 일괄 조회.
     * - 후기 이미지 중 좋아요 최다 1장 (단일 조회와 동일 산식: 좋아요 ↓, 최신 후기 ↓, 이미지 ↑)
     * - 후기 없거나 이미지 없는 음식점은 결과에서 빠짐 → 서비스에서 null 매핑
     * - PostgreSQL DISTINCT ON 으로 음식점당 1행만 반환
     */
    @Query(value = """
        SELECT DISTINCT ON (sub.restaurant_id)
            sub.restaurant_id AS restaurantId,
            sub.img_url       AS imgUrl
        FROM (
            SELECT
                r.restaurant_id,
                r.review_id,
                ri.img_id,
                ri.img_url,
                COUNT(l.like_id) AS like_count
            FROM reviews r
            JOIN review_imgs ri
                ON ri.review_id = r.review_id
            LEFT JOIN likes l
                ON l.review_id = r.review_id
            WHERE r.restaurant_id IN (:restaurantIds)
            GROUP BY r.restaurant_id, r.review_id, ri.img_id, ri.img_url
        ) sub
        ORDER BY sub.restaurant_id,
                 sub.like_count DESC,
                 sub.review_id DESC,
                 sub.img_id ASC
    """, nativeQuery = true)
    List<RestaurantRepresentativeImageProjection> findRepresentativeImageUrlsByRestaurantIds(
            @Param("restaurantIds") List<Long> restaurantIds
    );

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