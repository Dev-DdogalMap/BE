package com.ddogalmap.domain.reviews.repository;

import com.ddogalmap.domain.reviews.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    // 중복 누름 방지나 좋아요 취소를 위해 유저ID와 리뷰ID로 존재 여부를 찾는 메서드
    boolean existsByReviewReviewIdAndUserId(Long reviewId, Long userId);
}
