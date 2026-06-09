package com.ddogalmap.domain.badges.service;

import com.ddogalmap.domain.badges.entity.Badge;
import com.ddogalmap.domain.badges.entity.UserBadge;
import com.ddogalmap.domain.badges.enumtype.BadgeConditionType;
import com.ddogalmap.domain.badges.repository.BadgeFoodTypeRepository;
import com.ddogalmap.domain.badges.repository.BadgeRepository;
import com.ddogalmap.domain.badges.repository.UserBadgeRepository;
import com.ddogalmap.domain.chat.repository.DirectChatRoomRepository;
import com.ddogalmap.domain.reviews.entity.Review;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.domain.visit.dto.projection.VisitVerificationCountProjection;
import com.ddogalmap.domain.visit.repository.VisitVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BadgeGrantServiceImpl implements BadgeGrantService {

    private final BadgeRepository badgeRepository;
    private final BadgeFoodTypeRepository badgeFoodTypeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    private final ReviewRepository reviewRepository;
    private final VisitVerificationRepository visitVerificationRepository;
    private final DirectChatRoomRepository directChatRoomRepository;

    /**
     * 리뷰 관련 뱃지 검사
     */
    @Override
    public void checkReviewBadges(Long userId, Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        Long foodTypeId = review.getRestaurant().getFoodType().getFoodTypeId();

        Set<Long> ownedBadgeIds = getOwnedBadgeIds(userId);
        int totalReviewCount = reviewRepository.countByUserId(userId);

        grantBadgesByCondition(
                userId,
                BadgeConditionType.REVIEW_COUNT,
                totalReviewCount,
                ownedBadgeIds
        );

        grantFoodTypeReviewBadges(
                userId,
                foodTypeId,
                ownedBadgeIds
        );
    }

    /**
     * 방문 인증 관련 뱃지 검사
     */
    @Override
    public void checkVisitVerificationBadge(Long userId) {
        Set<Long> ownedBadgeIds = getOwnedBadgeIds(userId);

        VisitVerificationCountProjection count =
                visitVerificationRepository.countVisitBadgesByUserId(userId);

        grantBadgesByCondition(
                userId,
                BadgeConditionType.REGION_VERIFY_COUNT,
                count.getTotalCount(),
                ownedBadgeIds
        );

        grantBadgesByCondition(
                userId,
                BadgeConditionType.MORNING_VISIT_COUNT,
                count.getMorningCount(),
                ownedBadgeIds
        );

        grantBadgesByCondition(
                userId,
                BadgeConditionType.NIGHT_VISIT_COUNT,
                count.getNightCount(),
                ownedBadgeIds
        );

    }

    /**
     * 채팅 요청 수신 관련 뱃지 검사
     */
    @Override
    public void checkChatRequestReceivedBadge(Long userId) {
        Set<Long> ownedBadgeIds = getOwnedBadgeIds(userId);

        int receivedRequestCount =
                directChatRoomRepository.countByReceiverUserId(userId);

        grantBadgesByCondition(
                userId,
                BadgeConditionType.CHAT_REQUEST_RECEIVED,
                receivedRequestCount,
                ownedBadgeIds
        );
    }

    /**
     * 신규 유저 배지 검사
     */
    @Override
    public void grantNewUserBadge(Long userId) {
        Set<Long> ownedBadgeIds = getOwnedBadgeIds(userId);

        badgeRepository.findByConditionType(BadgeConditionType.NEW_USER)
                .stream()
                .findFirst()
                .ifPresent(badge -> {
                    if (!ownedBadgeIds.contains(badge.getBadgeId())) {
                        grantBadge(userId, badge, ownedBadgeIds);
                    }
                });
    }

    /**
     * 음식 카테고리 리뷰 뱃지 지급
     */
    private void grantFoodTypeReviewBadges(
            Long userId,
            Long foodTypeId,
            Set<Long> ownedBadgeIds
    ) {
        List<Badge> badges = badgeFoodTypeRepository
                .findBadgesByFoodTypeIdAndConditionType(
                        foodTypeId,
                        BadgeConditionType.FOOD_TYPE_REVIEW_COUNT
                );

        for (Badge badge : badges) {
            if (isAlreadyOwned(badge, ownedBadgeIds)) {
                continue;
            }

            int foodTypeReviewCount =
                    reviewRepository.countByUserIdAndBadgeFoodTypes(
                            userId,
                            badge.getBadgeId()
                    );

            if (isConditionSatisfied(badge, foodTypeReviewCount)) {
                grantBadge(userId, badge, ownedBadgeIds);
            }
        }
    }

    /**
     * 조건 타입별 뱃지 지급
     */
    private void grantBadgesByCondition(
            Long userId,
            BadgeConditionType conditionType,
            int count,
            Set<Long> ownedBadgeIds
    ) {
        List<Badge> badges = badgeRepository.findByConditionType(conditionType);

        for (Badge badge : badges) {
            if (isAlreadyOwned(badge, ownedBadgeIds)) {
                continue;
            }

            if (isConditionSatisfied(badge, count)) {
                grantBadge(userId, badge, ownedBadgeIds);
            }
        }
    }

    /**
     * 이미 획득한 뱃지인지 확인
     */
    private boolean isAlreadyOwned(
            Badge badge,
            Set<Long> ownedBadgeIds
    ) {
        return ownedBadgeIds.contains(badge.getBadgeId());
    }

    /**
     * 뱃지 획득 조건 충족 여부 확인
     */
    private boolean isConditionSatisfied(
            Badge badge,
            int count
    ) {
        return count >= badge.getConditionValue();
    }

    /**
     * 사용자에게 뱃지 지급
     */
    private void grantBadge(
            Long userId,
            Badge badge,
            Set<Long> ownedBadgeIds
    ) {
        User user = userRepository.getReferenceById(userId);

        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .build();

        try {
            userBadgeRepository.saveAndFlush(userBadge);
            ownedBadgeIds.add(badge.getBadgeId());

            if (user.getRepresentativeBadge() == null) {
                user.updateRepresentativeBadge(badge);
            }

            log.info(
                    "[Badge] 배지 획득. userId={}, badgeId={}, badgeName={}",
                    userId,
                    badge.getBadgeId(),
                    badge.getName()
            );
        } catch (DataIntegrityViolationException e) {
            ownedBadgeIds.add(badge.getBadgeId());
        }
    }

    /**
     * 사용자의 보유 뱃지 목록 조회
     */
    private Set<Long> getOwnedBadgeIds(Long userId) {
        return new HashSet<>(
                userBadgeRepository.findBadgeIdsByUserId(userId)
        );
    }
}