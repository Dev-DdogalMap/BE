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
    public ResponseEntity<String> addLike(@PathVariable Long reviewId,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal // 💡 보안 및 편의성을 위해 수정
    ) {
        likeService.addLike(reviewId, userPrincipal.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewId + "번 리뷰에 좋아요를 등록했습니다.");
    }
}