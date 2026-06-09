package com.ddogalmap.domain.chat.dto.groupChat.response;

import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;

import java.util.List;

public record ChatRoomMembersResponse(
        ChatRoomMemberRole currentUserRole,
        Integer participantCount,
        Integer maxParticipantCount,
        List<MemberDetailInfo> members
) {
}
