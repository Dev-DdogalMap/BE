package com.ddogalmap.domain.chat.dto.groupChat.response;

import java.time.LocalDateTime;

public record ChatRoomListThumbnailResponse(
        Long roomId,
        String roomImageUrl,
        String roomName,
        Integer participantCount,
        Integer maxParticipantCount,
        LocalDateTime createdAt,
        LocalDateTime latestMessageTime
) {
}
