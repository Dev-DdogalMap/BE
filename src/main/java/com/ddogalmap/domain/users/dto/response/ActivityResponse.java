package com.ddogalmap.domain.users.dto.response;

import com.ddogalmap.domain.badges.dto.response.BadgeInfo;
import com.ddogalmap.domain.levels.dto.response.LevelInfo;

public record ActivityResponse(
        LevelInfo level,
        BadgeInfo badges
) {
}
