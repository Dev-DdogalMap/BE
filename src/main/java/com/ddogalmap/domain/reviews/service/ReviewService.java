package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.reviews.dto.request.ReviewRequest;
import com.ddogalmap.domain.reviews.dto.response.ReviewResponse;
import com.ddogalmap.domain.reviews.entity.Review;
import com.ddogalmap.domain.reviews.entity.ReviewImg;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.domain.visit.entity.VisitVerification;
import com.ddogalmap.domain.visit.repository.VisitVerificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
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
    private final VisitVerificationRepository visitVerificationRepository;

    // 리뷰 생성
    @Transactional
    public Long createReviewWithVerification(Long visitVerificationId, Long userId, ReviewRequest request, List<MultipartFile> images) {
        // 1. 기준이 되는 방문 인증 데이터를 조회합니다.
        VisitVerification visitVerification = visitVerificationRepository.findById(visitVerificationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 방문 인증 내역입니다."));

        // [방어 코드] 현재 로그인한 유저가 해당 방문 인증의 소유자가 맞는지 검증합니다.
        if (!visitVerification.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("해당 방문 인증에 대한 리뷰 작성 권한이 없습니다.");
        }

        // [방어 코드] 이미 해당 방문 인증 건으로 작성된 리뷰가 있는지 확인합니다. (1건의 방문 = 1개의 리뷰)
        if (reviewRepository.existsByVisitVerification(visitVerification)) {
            throw new IllegalStateException("이미 해당 방문에 대해 작성된 리뷰가 존재합니다.");
        }

        // 2. 방문 인증 엔티티에서 맛집 ID를 추출합니다.
        Long restaurantId = visitVerification.getRestaurant().getRestaurantId();

        Review review = Review.builder()
                .score(request.score())
                .isRevisit(request.isRevisit())
                .content(request.content())
                .userId(userId)
                .restaurantId(restaurantId)
                .build();

        review.setVisitVerification(visitVerification);

        if (request.tags() != null) {
            request.tags().forEach(review::addTag);
        }

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
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

    /*@Transactional
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
    }*/

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

    /**
     * 💡 내가 작성한 후기 목록 조회 메서드 추가
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        // 1. 해당 유저 ID 조건으로 리포지토리에서 페이징 조회
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);

        // 2. 단일 유저이므로 전체를 맵으로 조회할 필요 없이, 해당 유저의 닉네임만 한 번 조회
        String nickname = userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("알 수 없는 유저");

        // 3. 엔티티를 ReviewResponse DTO 구조로 변환하여 반환
        return reviewPage.map(review -> new ReviewResponse(
                review.getReviewId(),
                nickname,
                review.getScore(),
                review.getContent(),
                review.getIsRevisit(),
                review.getCreatedAt(),
                review.getImages().stream()
                        .map(ReviewImg::getImgUrl)
                        .toList(),
                review.getTags().stream()
                        .map(tag -> tag.getContent())
                        .toList()
        ));
    }
}