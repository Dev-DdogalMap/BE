package com.ddogalmap.domain.chat.dto.response;

import java.time.LocalDateTime;

public record DirectChatRoomResponse(
        Long directChatRoomId,
        Long opponentUserId,
        String opponentNickname,
        String opponentProfileImageUrl,
        String lastMessage,
        LocalDateTime lastMessageAt,
        LocalDateTime createdAt
) {
}
