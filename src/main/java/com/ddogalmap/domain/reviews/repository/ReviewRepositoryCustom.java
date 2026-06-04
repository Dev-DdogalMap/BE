package com.ddogalmap.domain.reviews.repository;

import com.ddogalmap.domain.reviews.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ReviewRepositoryCustom {
    Slice<Review> findReviewsWithFilter(Long restaurantId, boolean hasImage, Pageable pageable);
}
