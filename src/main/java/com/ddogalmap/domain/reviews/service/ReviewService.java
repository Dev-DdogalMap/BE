package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.reviews.dto.request.ReviewRequest;
import com.ddogalmap.domain.reviews.entity.Review;
import com.ddogalmap.domain.reviews.entity.ReviewImg;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
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
    public Long createReview(ReviewRequest request, List<MultipartFile> images) {

        Review review = Review.builder()
                .score(request.score())
                .isRevisit(request.isRevisit())
                .content(request.content())
                .userId(1L)
                .restaurantId(1L)
                .build();

        // 2. 전달받은 태그 문자열들을 Review 엔티티에 연관관계 편의 메서드로 주입
        if (request.tags() != null) {
            request.tags().forEach(review::addTag);
        }

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String storeFilename = fileService.saveFile(image);
                String orgFilename = image.getOriginalFilename();
                ReviewImg reviewImg = ReviewImg.builder()
                        .imgUrl("/uploads/" + storeFilename)
                        .orgImgName(orgFilename)
                        .review(review)
                        .build();
                review.addImage(reviewImg); // 연관관계 편의 메서드 사용
            }
        }

        return reviewRepository.save(review).getReviewId();
    }
}