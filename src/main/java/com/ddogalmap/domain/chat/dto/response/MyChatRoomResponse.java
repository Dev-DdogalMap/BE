package com.ddogalmap.domain.chat.dto.response;

import java.time.LocalDateTime;

public record MyChatRoomResponse(
        Long directChatRoomId,
        Long targetUserId,
        String targetNickname,
        String targetProfileImageUrl,
        String lastMessage,
        LocalDateTime lastMessageAt,
        Integer unreadCount,
        LocalDateTime createdAt,
        String chatType  // "DIRECT" or "GROUP" 추가
) {
}
