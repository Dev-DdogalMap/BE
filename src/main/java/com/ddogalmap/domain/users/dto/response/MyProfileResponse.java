package com.ddogalmap.domain.users.dto.response;

import com.ddogalmap.domain.users.entity.User;

public record MyProfileResponse(
        String nickname,
        String profileImageUrl,
        String email
) {
    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getEmail()
        );
    }
}
