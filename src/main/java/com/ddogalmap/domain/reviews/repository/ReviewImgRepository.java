package com.ddogalmap.domain.reviews.repository;

import com.ddogalmap.domain.reviews.entity.ReviewImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewImgRepository extends JpaRepository<ReviewImg, Long> {
    Optional<ReviewImg> findFirstByReview_Restaurant_RestaurantIdOrderByImgIdAsc(
            Long restaurantId
    );

}
