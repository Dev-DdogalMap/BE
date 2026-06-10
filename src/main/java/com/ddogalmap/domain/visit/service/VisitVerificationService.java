package com.ddogalmap.domain.visit.service;

import com.ddogalmap.domain.badges.dto.VisitVerifiedEvent;
import com.ddogalmap.domain.levels.dto.LevelExpEvent;
import com.ddogalmap.domain.levels.enumtype.ActivityType;
import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.restaurants.repository.RestaurantRepository;
import com.ddogalmap.domain.reviews.dto.response.UnwrittenReviewResponseDto;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.domain.visit.dto.request.VisitVerificationRequest;
import com.ddogalmap.domain.visit.dto.response.VisitVerificationResponse;
import com.ddogalmap.domain.visit.entity.VisitVerification;
import com.ddogalmap.domain.visit.repository.VisitVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitVerificationService {

    private static final double VISIT_RADIUS_METER = 50.0;

    private final VisitVerificationRepository visitVerificationRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public VisitVerificationResponse verifyVisit(
            Long userId,
            VisitVerificationRequest request
    ) {
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new IllegalArgumentException("음식점을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Double storeLatitude = restaurantRepository.findLatitudeByRestaurantId(request.restaurantId());
        Double storeLongitude = restaurantRepository.findLongitudeByRestaurantId(request.restaurantId());

        if (storeLatitude == null || storeLongitude == null) {
            throw new IllegalStateException("음식점 위치 정보가 없습니다.");
        }

        Double distanceMeter = restaurantRepository.calculateDistanceMeter(
                request.restaurantId(),
                request.userLatitude(),
                request.userLongitude()
        );

        if (distanceMeter == null) {
            throw new IllegalStateException("거리 계산에 실패했습니다.");
        }

        if (distanceMeter > VISIT_RADIUS_METER) {
            throw new IllegalArgumentException("가게 반경 50m 이내에서만 방문 인증할 수 있습니다.");
        }

        VisitVerification visitVerification = VisitVerification.create(
                restaurant,
                user,
                request.userLatitude(),
                request.userLongitude(),
                storeLatitude,
                storeLongitude,
                distanceMeter,
                request.accuracyMeter()
        );

        VisitVerification saved = visitVerificationRepository.save(visitVerification);

        // 경험치 이벤트 발행
        eventPublisher.publishEvent(new LevelExpEvent(userId, ActivityType.VISIT_VERIFY, saved.getId()));
        eventPublisher.publishEvent(new VisitVerifiedEvent(user.getUserId()));

        return new VisitVerificationResponse(
                saved.getId(),
                restaurant.getRestaurantId(),
                saved.getDistanceMeter(),
                saved.getVerifiedAt()
        );
    }

    /**
     * 유저의 미작성 후기 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<UnwrittenReviewResponseDto> getUnwrittenReviews(Long userId, Pageable pageable) {
        // 1. Repository를 통해 미작성 방문 인증 엔티티 조회
        Page<VisitVerification> visitVerifications =
                visitVerificationRepository.findUnwrittenReviews(userId, pageable);

        // 2. 엔티티 페이지를 DTO 페이지로 변환하여 반환
        return visitVerifications.map(UnwrittenReviewResponseDto::from);
    }
}