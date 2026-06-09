package com.ddogalmap.domain.badges.dto.projection;

import com.ddogalmap.domain.badges.enumtype.BadgeConditionType;

import java.time.LocalDateTime;

public interface BadgeDetailProjection {

    Long getBadgeId();
    String getName();
    String getIconImage();
    Long getFoodTypeId();
    Boolean getAcquired();
    LocalDateTime getAcquiredAt();
    Long getConditionValue();
    BadgeConditionType getConditionType();
}
