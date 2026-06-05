package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.reviews.dto.request.ReviewRequest;
import com.ddogalmap.domain.reviews.dto.response.ReviewResponse;
import com.ddogalmap.domain.reviews.entity.Review;
import com.ddogalmap.domain.reviews.entity.ReviewImg;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final FileService fileService;
    private final UserRepository userRepository;

    @Transactional
    public Long createReview(Long restaurantId, Long userId, ReviewRequest request, List<MultipartFile> images) {
        Review review = Review.builder()
                .score(request.score())
                .isRevisit(request.isRevisit())
                .content(request.content())
                .userId(userId)
                .restaurantId(restaurantId)
                .build();

        // 전달받은 태그 문자열들을 Review 엔티티에 연관관계 편의 메서드로 주입
        if (request.tags() != null) {
            request.tags().forEach(review::addTag);
        }

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                // 💡 storeFilename에 전체 S3 URL 주소가 담겨옵니다.
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

        // N+1 문제를 방지하기 위해 조회된 리뷰들에서 고유한 userId 목록을 추출
        List<Long> userIds = reviewPage.getContent().stream()
                .map(Review::getUserId)
                .distinct()
                .toList();
        // 추출한 userId 목록으로 회원 데이터 일괄 조회 후 Map 구조로 변환 (Key: userId, Value: nickname)
        Map<Long, String> userNicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getNickname));

        // 트랜잭션 안에서 DTO로 변환하며 닉네임 매핑 수행
        return reviewPage.map(review -> {
            // Map에서 닉네임을 찾고, 만약 탈퇴 등의 이유로 없다면 기본값 처리
            String nickname = userNicknameMap.getOrDefault(review.getUserId(), "알 수 없는 유저");

            return new ReviewResponse(
                    review.getReviewId(),
                    nickname, // 💡 DTO에 조회한 닉네임 주입
                    review.getScore(),
                    review.getContent(),
                    review.getIsRevisit(),
                    review.getCreatedAt(),
                    review.getImages().stream()
                            .map(com.ddogalmap.domain.reviews.entity.ReviewImg::getImgUrl)
                            .toList(),
                    review.getTags().stream()
                            .map(tag -> tag.getContent())
                            .toList()
            );
        });
    }
}