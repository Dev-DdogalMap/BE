package com.ddogalmap.domain.levels.dto;

import com.ddogalmap.domain.levels.enumtype.ActivityType;

public record LevelExpEvent(
        Long userId,
        ActivityType activityType,
        Long referenceId
) {
}
