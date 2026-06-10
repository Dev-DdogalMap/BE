package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.restaurants.repository.RestaurantRepository; // 💡 가게 리포지토리 임포트 추가
import com.ddogalmap.domain.badges.dto.ReviewCreatedEvent;
import com.ddogalmap.domain.levels.dto.LevelExpEvent;
import com.ddogalmap.domain.levels.enumtype.ActivityType;
import com.ddogalmap.domain.restaurants.event.RestaurantStatsRefreshEvent;
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
import org.springframework.context.ApplicationEventPublisher;
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
    private final RestaurantRepository restaurantRepository; // 💡 의존성 추가
    private final ApplicationEventPublisher eventPublisher;

    // 리뷰 생성 로직 (기존과 동일)
    @Transactional
    public Long createReviewWithVerification(Long visitVerificationId, Long userId, ReviewRequest request, List<MultipartFile> images) {
        VisitVerification visitVerification = visitVerificationRepository.findById(visitVerificationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 방문 인증 내역입니다."));

        if (!visitVerification.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("해당 방문 인증에 대한 리뷰 작성 권한이 없습니다.");
        }

        if (reviewRepository.existsByVisitVerification(visitVerification)) {
            throw new IllegalStateException("이미 해당 방문에 대해 작성된 리뷰가 존재합니다.");
        }

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

        boolean hasImage = images != null && !images.isEmpty();

        // 이미지 저장
        if (hasImage) {
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

        Review savedReview = reviewRepository.save(review);

        // 경험치 이벤트 발행
        ActivityType activityType = hasImage
                ? ActivityType.REVIEW_PHOTO
                : ActivityType.REVIEW_WRITE;

        eventPublisher.publishEvent(new LevelExpEvent(userId, activityType, savedReview.getReviewId()));
        eventPublisher.publishEvent(new ReviewCreatedEvent(userId, savedReview.getReviewId()));
        // restaurant_stats 즉시 갱신 트리거 (AFTER_COMMIT + @Async 로 처리됨)
        eventPublisher.publishEvent(new RestaurantStatsRefreshEvent(List.of(restaurantId)));

        return savedReview.getReviewId();
    }

    // 가게별 리뷰 조회
    @Transactional(readOnly = true)
    public Slice<ReviewResponse> getReviewsByRestaurant(Long restaurantId, boolean hasImage, Pageable pageable) {
        Slice<Review> reviewPage = reviewRepository.findReviewsWithFilter(restaurantId, hasImage, pageable);

        // 💡 이 메서드 안의 모든 리뷰는 동일한 'restaurantId'를 가지므로, 단 한 번만 가게 이름을 조회하면 됩니다.
        String restaurantName = restaurantRepository.findById(restaurantId)
                .map(Restaurant::getPlaceName)
                .orElse("알 수 없는 가게");

        List<Long> userIds = reviewPage.getContent().stream()
                .map(Review::getUserId)
                .distinct()
                .toList();

        Map<Long, String> userNicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getNickname));

        return reviewPage.map(review -> {
            String nickname = userNicknameMap.getOrDefault(review.getUserId(), "알 수 없는 유저");

            return new ReviewResponse(
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
                            .toList(),
                    review.getLikes().size(),
                    restaurantName // 💡 생성자 마지막 파라미터로 주입
            );
        });
    }

    // 내가 작성한 후기 목록 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);

        String nickname = userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("알 수 없는 유저");

        // 💡 내가 작성한 리뷰들은 서로 다른 가게일 수 있으므로, N+1 문제를 막기 위해 인덱싱 리스트를 추출해 대량 조회(In Query) 처리합니다.
        List<Long> restaurantIds = reviewPage.getContent().stream()
                .map(Review::getRestaurantId)
                .distinct()
                .toList();

        Map<Long, String> restaurantNameMap = restaurantRepository.findAllById(restaurantIds).stream()
                .collect(Collectors.toMap(Restaurant::getRestaurantId, Restaurant::getPlaceName));

        return reviewPage.map(review -> {
            // Map에서 해당 리뷰의 restaurantId에 매칭되는 가게명을 찾습니다.
            String restaurantName = restaurantNameMap.getOrDefault(review.getRestaurantId(), "알 수 없는 가게");

            return new ReviewResponse(
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
                            .toList(),
                    review.getLikes().size(),
                    restaurantName // 💡 생성자 마지막 파라미터로 주입
            );
        });
    }
}