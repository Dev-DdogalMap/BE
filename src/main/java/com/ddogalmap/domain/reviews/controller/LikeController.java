package com.ddogalmap.domain.reviews.controller;

import com.ddogalmap.domain.reviews.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<String> addLike(@PathVariable Long reviewId) {
        // 테스트용
        Long temporaryUserId = 1L;

        likeService.addLike(reviewId, temporaryUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewId + "번 리뷰에 좋아요를 등록했습니다.");
    }
}