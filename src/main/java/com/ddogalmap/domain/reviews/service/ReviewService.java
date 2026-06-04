package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.reviews.dto.request.ReviewRequest;
import com.ddogalmap.domain.reviews.dto.response.ReviewResponse;
import com.ddogalmap.domain.reviews.entity.Review;
import com.ddogalmap.domain.reviews.entity.ReviewImg;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final FileService fileService;

    @Transactional
    public Long createReview(Long restaurantId, Long userId, ReviewRequest request, List<MultipartFile> images) {

        Review review = Review.builder()
                .score(request.score())
                .isRevisit(request.isRevisit())
                .content(request.content())
                .userId(userId)
                .restaurantId(restaurantId)
                .build();

        // 2. 전달받은 태그 문자열들을 Review 엔티티에 연관관계 편의 메서드로 주입
        if (request.tags() != null) {
            request.tags().forEach(review::addTag);
        }

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                // 💡 중요: 이제 storeFilename에 전체 S3 URL 주소가 담겨옵니다.
                String storeFilename = fileService.saveFile(image);
                String orgFilename = image.getOriginalFilename();

                ReviewImg reviewImg = ReviewImg.builder()
                        .imgUrl(storeFilename)
                        .orgImgName(orgFilename)
                        .review(review)
                        .build();
                review.addImage(reviewImg);
            }
        }

        return reviewRepository.save(review).getReviewId();
    }

    @Transactional(readOnly = true)
    public Slice<ReviewResponse> getReviewsByRestaurant(Long restaurantId, boolean hasImage, Pageable pageable) {

        // QueryDSL 커스텀 메서드 호출
        Slice<Review> reviewPage = reviewRepository.findReviewsWithFilter(restaurantId, hasImage, pageable);

        // 트랜잭션 안에서 DTO로 변환
        return reviewPage.map(review -> new ReviewResponse(
                review.getReviewId(),
                review.getScore(),
                review.getContent(),
                review.getIsRevisit(),
                review.getCreatedAt(), // BaseEntity로부터 상속받은 생성일자
                review.getImages().stream()
                        .map(com.ddogalmap.domain.reviews.entity.ReviewImg::getImgUrl)
                        .toList(),
                review.getTags().stream()
                        .map(tag -> tag.getContent())
                        .toList()
        ));
    }
}