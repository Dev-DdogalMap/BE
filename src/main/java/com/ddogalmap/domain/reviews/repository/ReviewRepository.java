package com.ddogalmap.domain.reviews.repository;

import com.ddogalmap.domain.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    // 특정 식당의 리뷰를 최신슨 둥의 조건에 맞춰 페이징 조회
    Slice<Review> findByRestaurantId(Long restaurantId, Pageable pageable);
}