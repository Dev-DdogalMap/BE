package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.reviews.entity.Like;
import com.ddogalmap.domain.reviews.entity.Review;
import com.ddogalmap.domain.reviews.repository.LikeRepository;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public boolean toggleLike(Long reviewId, Long userId) {
        Optional<Like> existingLike = likeRepository.findByReviewReviewIdAndUserId(reviewId, userId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return false;
        } else {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

            Like like = Like.builder()
                    .review(review)
                    .userId(userId)
                    .build();

            likeRepository.save(like);
            return true;
        }
    }
}