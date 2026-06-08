package com.ddogalmap.domain.levels.dto.projection;

import java.time.LocalDateTime;

public interface LevelHistoryProjection {

    Long getHistoryId();
    String getActivityType();
    Integer getExpAmount();
    Integer getLevel();
    String getLevelName();
    LocalDateTime getCreatedAt();
}
