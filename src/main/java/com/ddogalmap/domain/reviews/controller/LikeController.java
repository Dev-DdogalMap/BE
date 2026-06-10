package com.ddogalmap.domain.reviews.controller;

import com.ddogalmap.domain.reviews.dto.request.LikeRequest;
import com.ddogalmap.domain.reviews.service.LikeService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable("reviewId") Long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 1. 서비스 호출 여부 반드시 확인
        boolean isLiked = likeService.toggleLike(reviewId, userPrincipal.userId());

        // 2. 현재 상태가 좋아요 상태(true)인지 취소 상태(false)인지 반환
        return ResponseEntity.ok(isLiked);
    }
}