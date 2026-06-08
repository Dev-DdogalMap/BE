package com.ddogalmap.domain.chat.dto.groupChat.request;

public record UpdateChatRoomRequest(
        String roomName,
        String region,
        Long foodTypeId,
        Integer maxParticipantCount,
        String imageKey
) {
}
