package com.ddogalmap.domain.users.dto.response;

import com.ddogalmap.domain.badges.dto.response.BadgeDetailResponse;
import com.ddogalmap.domain.badges.dto.response.BadgeResponse;
import com.ddogalmap.domain.levels.dto.response.LevelHistoryResponse;
import com.ddogalmap.domain.levels.dto.response.LevelInfo;

import java.util.List;

public record ActivityDetailResponse(
        LevelInfo level,
        BadgeResponse representativeBadge,
        List<BadgeDetailResponse> badges,
        List<LevelHistoryResponse> levelHistories
) {
}
