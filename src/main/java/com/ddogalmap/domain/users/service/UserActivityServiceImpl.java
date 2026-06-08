package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.badges.dto.BadgeProgressSummary;
import com.ddogalmap.domain.badges.dto.projection.BadgeDetailProjection;
import com.ddogalmap.domain.badges.dto.response.BadgeDetailResponse;
import com.ddogalmap.domain.badges.dto.response.BadgeInfo;
import com.ddogalmap.domain.badges.dto.response.BadgeResponse;
import com.ddogalmap.domain.badges.entity.Badge;
import com.ddogalmap.domain.badges.entity.UserBadge;
import com.ddogalmap.domain.badges.repository.BadgeFoodTypeRepository;
import com.ddogalmap.domain.badges.repository.BadgeRepository;
import com.ddogalmap.domain.badges.repository.UserBadgeRepository;
import com.ddogalmap.domain.badges.service.BadgeProgressCalculator;
import com.ddogalmap.domain.chat.repository.DirectChatRoomRepository;
import com.ddogalmap.domain.levels.dto.response.LevelHistoryResponse;
import com.ddogalmap.domain.levels.dto.response.LevelInfo;
import com.ddogalmap.domain.levels.entity.Level;
import com.ddogalmap.domain.levels.entity.UserLevel;
import com.ddogalmap.domain.levels.repository.LevelRepository;
import com.ddogalmap.domain.levels.repository.UserLevelHistoryRepository;
import com.ddogalmap.domain.levels.repository.UserLevelRepository;
import com.ddogalmap.domain.reviews.dto.projection.FoodTypeReviewCountProjection;
import com.ddogalmap.domain.reviews.repository.ReviewRepository;
import com.ddogalmap.domain.users.dto.request.RepresentativeBadgeUpdateRequest;
import com.ddogalmap.domain.users.dto.response.ActivityDetailResponse;
import com.ddogalmap.domain.users.dto.response.ActivityResponse;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.domain.visit.repository.VisitVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityServiceImpl implements UserActivityService {

    private final UserRepository userRepository;
    private final UserLevelRepository userLevelRepository;
    private final LevelRepository levelRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final UserLevelHistoryRepository userLevelHistoryRepository;

    private final ReviewRepository reviewRepository;
    private final VisitVerificationRepository visitVerificationRepository;
    private final DirectChatRoomRepository directChatRoomRepository;
    private final BadgeProgressCalculator badgeProgressCalculator;
    private final BadgeFoodTypeRepository badgeFoodTypeRepository;

    @Transactional
    public BadgeResponse updateRepresentativeBadge(
            Long userId,
            RepresentativeBadgeUpdateRequest request
    ) {
        log.info("[UserActivityService] 대표 뱃지 변경 시작 - userId={}, badgeId={}", userId, request.badgeId());

        User user = userRepository.findByIdWithRepresentativeBadge(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!userBadgeRepository.existsByBadge_BadgeIdAndUser_UserId(
                request.badgeId(),
                userId
        )) {
            throw new IllegalArgumentException("보유하지 않은 뱃지 입니다.");
        }

        Badge badge = badgeRepository.findById(request.badgeId())
                .orElseThrow(() -> new IllegalArgumentException("뱃지를 찾을 수 없습니다."));

        user.updateRepresentativeBadge(badge);

        log.info("[UserActivityService] 대표 뱃지 변경 완료 - userId={}, badgeId={}", userId, request.badgeId());

        return BadgeResponse.from(badge);
    }

    public ActivityResponse getMyActivity(Long userId) {

        log.info("[UserActivityService] 활동 내역 조회 시작 - userId={}", userId);

        User user = userRepository.findByIdWithRepresentativeBadge(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserLevel userLevel = userLevelRepository.findByUserIdWithLevel(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 레벨 정보를 찾을 수 없습니다."));


        LevelInfo levelInfo = createLevelInfo(userLevel);
        BadgeInfo badgeInfo = createBadgeInfo(user, userId);

        log.info("[UserActivityService] 활동 내역 조회 성공 - userId={}, level={}, exp={}", userId, levelInfo.currentLevel(), levelInfo.currentExp());

        return new ActivityResponse(levelInfo, badgeInfo);
    }

    public ActivityDetailResponse getMyActivityDetail(Long userId) {
        log.info("[UserActivityService] 활동 내역 상세 조회 시작 - userId={}", userId);

        User user = userRepository.findByIdWithRepresentativeBadge(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserLevel userLevel = userLevelRepository.findByUserIdWithLevel(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 레벨 정보를 찾을 수 없습니다."));

        // 1. 사용자 레벨 정보 조회
        LevelInfo levelInfo = createLevelInfo(userLevel);

        // 2. 사용자 대표 뱃지 정보 조회
        BadgeResponse representativeBadge = BadgeResponse.from(user.getRepresentativeBadge());

        // 3. 뱃지 정보 전체 조회
        List<BadgeDetailResponse> badges = createBadgeDetails(userId);

        // 4. 경험치 history 조회
        List<LevelHistoryResponse> levelHistories = userLevelHistoryRepository
                .findRecentLevelHistories(userId, PageRequest.of(0, 10))
                .stream()
                .map(LevelHistoryResponse::from)
                .toList();

        log.info("[UserActivityService] 활동 내역 상세 조회 성공 - userId={}, badgeCount={}, historyCount={}", userId, badges.size(), levelHistories.size());

        return new ActivityDetailResponse(
                levelInfo,
                representativeBadge,
                badges,
                levelHistories
        );
    }

    // 뱃지 정보 전체 조회
    private List<BadgeDetailResponse> createBadgeDetails(Long userId) {
        // 1. 전체 뱃지 + 획득 여부 한 번에 조회 (LEFT JOIN)
        List<BadgeDetailProjection> badgeProjections =
                badgeRepository.findBadgeDetailsByUserId(userId);

        // 2. BadgeProgressSummary 생성
        BadgeProgressSummary summary = createBadgeProgressSummary(userId);

        // 3. 뱃지-음식유형 매핑
        Map<Long, List<Long>> badgeFoodTypeMap = badgeFoodTypeRepository
                .findAllWithBadgeAndFoodType()
                .stream()
                .collect(Collectors.groupingBy(
                        bft -> bft.getBadge().getBadgeId(),
                        Collectors.mapping(
                                bft -> bft.getFoodType().getFoodTypeId(),
                                Collectors.toList()
                        )
                ));

        return badgeProjections.stream()
                .map(projection -> {
                    int remainingCount = badgeProgressCalculator.calculateRemainingCount(
                            projection, summary, badgeFoodTypeMap);
                    return BadgeDetailResponse.from(projection, remainingCount);
                })
                .sorted(Comparator
                        .comparing(BadgeDetailResponse::acquired).reversed()  // 획득한 뱃지 먼저
                        .thenComparing(b -> b.acquired()
                                        ? b.acquiredAt()          // 획득한 뱃지 → 최신순
                                        : null,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparingInt(b -> b.acquired()
                                ? 0
                                : b.remainingCount()))    // 미획득 뱃지 → 남은 횟수 오름차순
                .toList();
    }

    private BadgeProgressSummary createBadgeProgressSummary(Long userId) {
        Map<Long, Integer> foodTypeReviewCounts = reviewRepository
                .countReviewsGroupByFoodType(userId)
                .stream()
                .collect(Collectors.toMap(
                        FoodTypeReviewCountProjection::getFoodTypeId,
                        FoodTypeReviewCountProjection::getReviewCount
                ));

        int totalReviewCount = foodTypeReviewCounts.values().stream()
                .mapToInt(i -> i).sum();

        return new BadgeProgressSummary(
                totalReviewCount,
                visitVerificationRepository.countVisitBadgesByUserId(userId),
                directChatRoomRepository.countByReceiverUserId(userId),
                foodTypeReviewCounts
        );
    }

    /**
     * 레벨 정보 생성
     */
    private LevelInfo createLevelInfo(UserLevel userLevel) {
        Level currentLevel = userLevel.getLevel();
        int currentExp = userLevel.getExp();

        Level nextLevel = levelRepository.findByLevel(currentLevel.getLevel() + 1)
                .orElse(null);

        // 다음 레벨이 없는 경우
        if (nextLevel == null) {
            return new LevelInfo(
                    currentLevel.getLevel(),
                    currentLevel.getName(),
                    currentExp,
                    0,
                    100
            );
        }

        int currentRequiredExp = currentLevel.getRequiredExp();
        int nextRequiredExp = nextLevel.getRequiredExp();

        int remainingExpToNextLevel = Math.max(nextRequiredExp - currentExp, 0);

        int progressPercent = calculateProgressPercent(
                currentExp,
                currentRequiredExp,
                nextRequiredExp
        );

        return new LevelInfo(
                currentLevel.getLevel(),
                currentLevel.getName(),
                currentExp,
                remainingExpToNextLevel,
                progressPercent
        );
    }

    /**
     * 현재 레벨 구간 기준 진행률 계산
     */
    private int calculateProgressPercent(
            int currentExp,
            int currentRequiredExp,
            int nextRequiredExp
    ) {
        int requiredExpGap = nextRequiredExp - currentRequiredExp;

        if (requiredExpGap <= 0) {
            return 100;
        }

        int gainedExpInCurrentLevel = currentExp - currentRequiredExp;
        int progressPercent = gainedExpInCurrentLevel * 100 / requiredExpGap;

        return Math.max(0, Math.min(progressPercent, 100));
    }


    /**
     * 대표 뱃지 및 최근 획득 뱃지 정보 생성
     */
    private BadgeInfo createBadgeInfo(User user, Long userId) {
        BadgeResponse representativeBadge =
                BadgeResponse.from(user.getRepresentativeBadge());

        List<BadgeResponse> recentBadges =
                userBadgeRepository
                        .findRecentByUserId(userId, PageRequest.of(0, 3))
                        .stream()
                        .map(UserBadge::getBadge)
                        .map(BadgeResponse::from)
                        .toList();

        return new BadgeInfo(
                representativeBadge,
                recentBadges
        );
    }


}
