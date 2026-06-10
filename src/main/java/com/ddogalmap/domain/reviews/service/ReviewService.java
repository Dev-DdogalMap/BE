package com.ddogalmap.domain.reviews.service;

import com.ddogalmap.domain.levels.repository.UserLevelRepository;
import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.restaurants.repository.RestaurantRepository;
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

import java.util.HashMap;
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
    private final RestaurantRepository restaurantRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserLevelRepository userLevelRepository;

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

        ActivityType activityType = hasImage
                ? ActivityType.REVIEW_PHOTO
                : ActivityType.REVIEW_WRITE;

        eventPublisher.publishEvent(new LevelExpEvent(userId, activityType, savedReview.getReviewId()));
        eventPublisher.publishEvent(new ReviewCreatedEvent(userId, savedReview.getReviewId()));
        eventPublisher.publishEvent(new RestaurantStatsRefreshEvent(List.of(restaurantId)));

        return savedReview.getReviewId();
    }

    // 가게별 리뷰 조회
    @Transactional(readOnly = true)
    public Slice<ReviewResponse> getReviewsByRestaurant(Long restaurantId, boolean hasImage, Pageable pageable) {
        Slice<Review> reviewPage = reviewRepository.findReviewsWithFilter(restaurantId, hasImage, pageable);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 가게입니다."));
        String restaurantName = restaurant.getPlaceName();

        List<Long> userIds = reviewPage.getContent().stream()
                .map(Review::getUserId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // 💡 해결 지점: Map의 단언 타입을 Integer에서 Level 엔티티 객체 타입으로 수정
        Map<Long, com.ddogalmap.domain.levels.entity.Level> userLevelMap = new HashMap<>();
        for (Long uid : userIds) {
            userLevelRepository.findByUserIdWithLevel(uid).ifPresent(ul -> {
                userLevelMap.put(uid, ul.getLevel()); // .getLevel() 엔티티 자체를 바인딩
            });
        }

        return reviewPage.map(review -> {
            User user = userMap.get(review.getUserId());
            String nickname = (user != null) ? user.getNickname() : "알 수 없는 유저";

            com.ddogalmap.domain.levels.entity.Level levelEntity = userLevelMap.get(review.getUserId());
            Integer level = (levelEntity != null) ? levelEntity.getLevel() : 1;
            String levelName = (levelEntity != null) ? levelEntity.getName() : "맛집 새내기";

            boolean isLocal = false;
            if (user != null && user.getRegion() != null) {
                String userRegion = user.getRegion();
                boolean isInRoadAddress = restaurant.getRoadAddressName() != null && restaurant.getRoadAddressName().contains(userRegion);
                boolean isInAddress = restaurant.getAddressName() != null && restaurant.getAddressName().contains(userRegion);
                isLocal = isInRoadAddress || isInAddress;
            }

            return new ReviewResponse(
                    review.getReviewId(),
                    nickname,
                    review.getScore(),
                    review.getContent(),
                    review.getIsRevisit(),
                    review.getCreatedAt(),
                    review.getImages().stream().map(ReviewImg::getImgUrl).toList(),
                    review.getTags().stream().map(tag -> tag.getContent()).toList(),
                    review.getLikes().size(),
                    restaurantName,
                    level,
                    levelName,
                    isLocal
            );
        });
    }

    // 내가 작성한 후기 목록 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
        String nickname = user.getNickname();

        // 💡 추가 수정: 내 리뷰 조회 시에도 등급 문자열(Name)을 함께 추출하도록 보완
        com.ddogalmap.domain.levels.entity.Level myLevelEntity = userLevelRepository.findByUserIdWithLevel(userId)
                .map(ul -> ul.getLevel())
                .orElse(null);

        Integer myLevel = (myLevelEntity != null) ? myLevelEntity.getLevel() : 1;
        String myLevelName = (myLevelEntity != null) ? myLevelEntity.getName() : "맛집 새내기";

        List<Long> restaurantIds = reviewPage.getContent().stream()
                .map(Review::getRestaurantId)
                .distinct()
                .toList();

        Map<Long, Restaurant> restaurantMap = restaurantRepository.findAllById(restaurantIds).stream()
                .collect(Collectors.toMap(Restaurant::getRestaurantId, r -> r));

        return reviewPage.map(review -> {
            Restaurant restaurant = restaurantMap.get(review.getRestaurantId());
            String restaurantName = (restaurant != null) ? restaurant.getPlaceName() : "알 수 없는 가게";

            boolean isLocal = false;
            if (user.getRegion() != null && restaurant != null) {
                String userRegion = user.getRegion();
                boolean isInRoadAddress = restaurant.getRoadAddressName() != null && restaurant.getRoadAddressName().contains(userRegion);
                boolean isInAddress = restaurant.getAddressName() != null && restaurant.getAddressName().contains(userRegion);
                isLocal = isInRoadAddress || isInAddress;
            }

            return new ReviewResponse(
                    review.getReviewId(),
                    nickname,
                    review.getScore(),
                    review.getContent(),
                    review.getIsRevisit(),
                    review.getCreatedAt(),
                    review.getImages().stream().map(ReviewImg::getImgUrl).toList(),
                    review.getTags().stream().map(tag -> tag.getContent()).toList(),
                    review.getLikes().size(),
                    restaurantName,
                    myLevel,
                    myLevelName,
                    isLocal
            );
        });
    }
}