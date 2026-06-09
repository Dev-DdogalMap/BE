package com.ddogalmap.domain.levels.dto.response;

public record LevelInfo(
        Integer currentLevel,
        String currentLevelName,
        Integer currentExp,
        Integer remainingExpToNextLevel,
        Integer progressPercent
) {
}
