package com.ddogalmap.domain.levels.service;

import com.ddogalmap.domain.levels.enumtype.ActivityType;

public interface LevelService {
    void addExp(Long userId, ActivityType activityType, Long referenceId);
}
