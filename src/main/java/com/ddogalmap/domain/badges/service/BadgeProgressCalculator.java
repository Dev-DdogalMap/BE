package com.ddogalmap.domain.badges.service;

import com.ddogalmap.domain.badges.dto.BadgeProgressSummary;
import com.ddogalmap.domain.badges.dto.projection.BadgeDetailProjection;
import com.ddogalmap.domain.badges.enumtype.BadgeConditionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BadgeProgressCalculator {

    public int calculateRemainingCount(
            BadgeDetailProjection projection,
            BadgeProgressSummary summary,
            Map<Long, List<Long>> badgeFoodTypeMap
    ) {
        if (projection.getAcquired()) return 0;

        int currentCount = calculateCurrentCount(
                projection.getBadgeId(),
                projection.getConditionType(),
                summary,
                badgeFoodTypeMap
        );

        return (int) Math.max(projection.getConditionValue() - currentCount, 0L);
    }

    private int calculateCurrentCount(
            Long badgeId,
            BadgeConditionType conditionType,
            BadgeProgressSummary summary,
            Map<Long, List<Long>> badgeFoodTypeMap
    ) {
        return switch (conditionType) {
            case REVIEW_COUNT -> summary.totalReviewCount();
            case FOOD_TYPE_REVIEW_COUNT ->
                    badgeFoodTypeMap.getOrDefault(badgeId, List.of())
                            .stream()
                            .mapToInt(summary::getFoodTypeReviewCount)
                            .sum();
            case REGION_VERIFY_COUNT  -> summary.visitCount().getTotalCount();
            case MORNING_VISIT_COUNT  -> summary.visitCount().getMorningCount();
            case NIGHT_VISIT_COUNT    -> summary.visitCount().getNightCount();
            case CHAT_REQUEST_RECEIVED -> summary.receivedChatRequestCount();
        };
    }
}
