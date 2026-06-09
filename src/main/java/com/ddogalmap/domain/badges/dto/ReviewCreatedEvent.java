package com.ddogalmap.domain.badges.dto;

public record ReviewCreatedEvent(
        Long userId,
        Long reviewId
) {
}
