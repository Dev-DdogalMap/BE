package com.ddogalmap.domain.badges.dto.response;

import java.util.List;

public record BadgeInfo(
        BadgeResponse representativeBadge,
        List<BadgeResponse> recentBadges
) {
}
