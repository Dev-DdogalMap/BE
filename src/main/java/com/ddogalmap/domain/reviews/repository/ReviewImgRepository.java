package com.ddogalmap.domain.reviews.repository;

import com.ddogalmap.domain.reviews.entity.ReviewImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewImgRepository extends JpaRepository<ReviewImg, Long> {

    @Query(value = """
        SELECT ri.img_url
        FROM review_imgs ri
        WHERE ri.review_id = (
            SELECT r.review_id
            FROM reviews r
            LEFT JOIN likes l
                ON l.review_id = r.review_id
            WHERE r.restaurant_id = :restaurantId
            GROUP BY r.review_id
            ORDER BY COUNT(l.like_id) DESC,
                     r.review_id DESC
            LIMIT 1
        )
        ORDER BY ri.img_id
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findTopLikedReviewImageUrl(@Param("restaurantId") Long restaurantId);
}
