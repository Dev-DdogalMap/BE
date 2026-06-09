package com.ddogalmap.domain.badges.dto;


import com.ddogalmap.domain.visit.dto.projection.VisitVerificationCountProjection;

import java.util.Map;

public record BadgeProgressSummary(
        int totalReviewCount,
        VisitVerificationCountProjection visitCount,
        int receivedChatRequestCount,
        Map<Long, Integer> foodTypeReviewCounts
) {

    public int getFoodTypeReviewCount(Long foodTypeId) {
        return foodTypeReviewCounts.getOrDefault(foodTypeId, 0);
    }
}
