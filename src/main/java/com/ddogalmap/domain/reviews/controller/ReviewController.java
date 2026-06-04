package com.ddogalmap.domain.reviews.controller;

import com.ddogalmap.domain.reviews.dto.request.ReviewRequest;
import com.ddogalmap.domain.reviews.dto.response.ReviewResponse;
import com.ddogalmap.domain.reviews.service.ReviewService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "Review", description = "음식점 후기 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "리뷰 등록")
    @PostMapping(value = "/restaurants/{restaurantId}/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createReview(
            @PathVariable("restaurantId") Long restaurantId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestPart("review") ReviewRequest request,
            @RequestPart(value = "images", required = false)List<MultipartFile> images) {

        Long reviewId = reviewService.createReview(restaurantId, userPrincipal.userId(), request, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("리뷰가 성공적으로 등록되었습니다. ID: " + reviewId);
    }

    @Operation(summary = "음식점별 리뷰 목록 조회")
    @GetMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<Slice<ReviewResponse>> getReviewsByRestaurant(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestParam(value = "hasImage", required = false, defaultValue = "false") boolean hasImage,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Slice<ReviewResponse> reviews = reviewService.getReviewsByRestaurant(restaurantId, hasImage, pageable);
        return ResponseEntity.ok(reviews);
    }

}