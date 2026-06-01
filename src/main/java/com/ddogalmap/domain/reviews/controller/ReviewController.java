package com.ddogalmap.domain.reviews.controller;

import com.ddogalmap.domain.reviews.dto.request.ReviewRequest;
import com.ddogalmap.domain.reviews.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Review", description = "음식점 후기 관련 API")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<String> createReview(@Valid @RequestBody ReviewRequest request) {
        Long reviewId = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("리뷰가 성공적으로 등록되었습니다. ID: " + reviewId);
    }
}