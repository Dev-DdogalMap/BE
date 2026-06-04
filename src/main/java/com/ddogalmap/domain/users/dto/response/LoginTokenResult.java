package com.ddogalmap.domain.users.dto.response;

//서버 내부 전달용
public record LoginTokenResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String nickname,
        String profileImageUrl
) {
}