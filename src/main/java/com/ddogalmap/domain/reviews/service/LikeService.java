package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.reviews.entity.Like;
import com.ddogalmap.domain.reviews.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public void addLike(Long reviewId, Long userId) {
        // [선택 사항] 이미 좋아요를 누른 상태인지 검증 (UNIQUE 제약 조건 위반 방지)
        if (likeRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            throw new IllegalStateException("이미 좋아요를 누른 리뷰입니다.");
        }

        Like like = Like.builder()
                .reviewId(reviewId)
                .userId(userId)
                .build();

        likeRepository.save(like);
    }
}