package com.ddogalmap.domain.chat.dto.groupChat.response;

import java.util.List;

public record ChatRoomMembersResponse(
        Integer participantCount,
        Integer maxParticipantCount,
        List<MemberDetailInfo> members
) {
}
