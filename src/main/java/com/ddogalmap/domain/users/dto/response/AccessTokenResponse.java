package com.ddogalmap.domain.users.dto.response;

// 프론트 응답용
public record AccessTokenResponse(
        String accessToken,
        Long userId,
        String nickname,
        String profileImageUrl
) {
}