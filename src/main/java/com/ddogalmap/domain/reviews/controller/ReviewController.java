package com.ddogalmap.domain.reviews.controller;

import com.ddogalmap.domain.reviews.dto.request.ReviewRequest;
import com.ddogalmap.domain.reviews.dto.response.ReviewResponse;
import com.ddogalmap.domain.reviews.dto.response.UnwrittenReviewResponseDto;
import com.ddogalmap.domain.reviews.service.ReviewService;
import com.ddogalmap.domain.visit.service.VisitVerificationService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final VisitVerificationService visitVerificationService;

    @Operation(summary = "리뷰 등록 (방문 인증 연동)")
    // 💡 기존 /restaurants/{restaurantId}/review 에서 변경
    @PostMapping(value = "/visit-verifications/{visitVerificationId}/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createReview(
            @PathVariable("visitVerificationId") Long visitVerificationId, // 맛집 ID 대신 방문인증 ID를 수신
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestPart("review") ReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        reviewService.createReviewWithVerification(visitVerificationId, userPrincipal.userId(), request, images);

        // 💡 ID 노출을 제거하고 토스트 문구로 바로 쓸 수 있는 메시지만 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("리뷰가 성공적으로 등록되었습니다.");
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

    @Operation(summary = "내가 작성한 리뷰 목록 조회")
    @GetMapping("/my/reviews") // 1. /api 중복 경로 제거하여 /api/my/reviews 로 매핑 완료
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal, // 2. 프로젝트 표준인 UserPrincipal로 변경
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = userPrincipal.userId(); // 3. 인자 추출 방식 변경
        Page<ReviewResponse> myReviews = reviewService.getMyReviews(userId, pageable);
        return ResponseEntity.ok(myReviews);
    }

    @Operation(summary = "내가 방문했으나 후기를 작성하지 않은 매장 목록 조회")
    @GetMapping("/my/unwritten-reviews") // 1. 프론트엔드 호출 경로에 맞게 /my 추가
    public ResponseEntity<Page<UnwrittenReviewResponseDto>> getUnwrittenReviews(
            // 2. 프로젝트 표준인 UserPrincipal 객체로 변경
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 5, sort = "verifiedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 3. userPrincipal.userId()를 호출하여 식별자 추출
        Page<UnwrittenReviewResponseDto> unwrittenReviews =
                visitVerificationService.getUnwrittenReviews(userPrincipal.userId(), pageable);

        return ResponseEntity.ok(unwrittenReviews);
    }
}