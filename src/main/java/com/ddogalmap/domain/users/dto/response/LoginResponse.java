package com.ddogalmap.domain.users.dto.response;

public record LoginResponse(
        String accessToken,
        Long userId,
        String nickname,
        String profileImageUrl
) {
}