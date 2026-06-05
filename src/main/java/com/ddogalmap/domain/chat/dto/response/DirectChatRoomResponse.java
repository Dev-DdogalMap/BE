package com.ddogalmap.domain.chat.dto.response;

import java.time.LocalDateTime;

public record DirectChatRoomResponse(
        Long directChatRoomId,
        Long targetUserId,
        String targetNickname,
        String targetProfileImageUrl,
        String lastMessage,
        LocalDateTime lastMessageAt,
        Integer unreadCount,
        LocalDateTime createdAt
) {
}
