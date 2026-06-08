package com.ddogalmap.domain.levels.dto.response;


import com.ddogalmap.domain.levels.dto.projection.LevelHistoryProjection;

import java.time.LocalDateTime;

public record LevelHistoryResponse(
        Long historyId,
        String activityType,
        Integer expAmount,
        Integer level,
        String levelName,
        LocalDateTime createdAt
) {

    public static LevelHistoryResponse from(LevelHistoryProjection projection) {
        return new LevelHistoryResponse(
                projection.getHistoryId(),
                projection.getActivityType(),
                projection.getExpAmount(),
                projection.getLevel(),
                projection.getLevelName(),
                projection.getCreatedAt()
        );
    }
}