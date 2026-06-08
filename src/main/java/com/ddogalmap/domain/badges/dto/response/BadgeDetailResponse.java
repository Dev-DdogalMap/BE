package com.ddogalmap.domain.badges.dto.response;

import com.ddogalmap.domain.badges.dto.projection.BadgeDetailProjection;

import java.time.LocalDateTime;

public record BadgeDetailResponse(
        Long badgeId,
        String name,
        String iconImage,
        Boolean acquired,
        LocalDateTime acquiredAt,
        Integer remainingCount

) {

    public static BadgeDetailResponse from(
            BadgeDetailProjection projection,
            int remainingCount
    ) {
        return new BadgeDetailResponse(
                projection.getBadgeId(),
                projection.getName(),
                projection.getIconImage(),
                projection.getAcquired(),
                projection.getAcquiredAt(),
                remainingCount
        );
    }
}
