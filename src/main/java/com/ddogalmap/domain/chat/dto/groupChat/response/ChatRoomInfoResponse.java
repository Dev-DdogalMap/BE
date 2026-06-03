package com.ddogalmap.domain.chat.dto.groupChat.response;

import java.util.List;

public record ChatRoomInfoResponse(
        String roomImage,
        String roomName,
        Integer participantCount,
        Integer maxParticipantCount,
        String category,
        String region,
        List<MemberInfo> members
) {
}
