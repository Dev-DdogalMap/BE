package com.ddogalmap.domain.badges.dto.response;

import com.ddogalmap.domain.badges.entity.Badge;

public record BadgeResponse(
        Long badgeId,
        String name,
        String iconImage
) {
    public static BadgeResponse from(Badge badge) {
        if (badge == null) {
            return null;
        }

        return new BadgeResponse(
                badge.getBadgeId(),
                badge.getName(),
                badge.getIconImage()
        );
    }
}