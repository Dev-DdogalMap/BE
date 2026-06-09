package com.ddogalmap.domain.chat.dto.groupChat.response;

import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;

public record MemberDetailInfo(
        Long userId,
        String userProfileImage,
        String userName,
        Integer userLevel,
        ChatRoomMemberRole userRole
) {
}
