package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.reviews.entity.Like;
import com.ddogalmap.domain.reviews.entity.Review;
import com.ddogalmap.domain.reviews.repository.LikeRepository;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository; // 💡 리뷰 조회를 위해 주입 추가

    @Transactional
    public void addLike(Long reviewId, Long userId) {
        // 1. 해당 리뷰가 존재하는지 먼저 조회합니다.
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 리뷰입니다."));

        // 2. 이미 좋아요를 누른 상태인지 검증
        if (likeRepository.existsByReviewReviewIdAndUserId(reviewId, userId)) {
            throw new IllegalStateException("이미 좋아요를 누른 리뷰입니다.");
        }

        // 3. 빌더에 조회한 review 객체를 반드시 주입합니다.
        Like like = Like.builder()
                .userId(userId)
                .review(review)
                .build();

        likeRepository.save(like);
    }
}