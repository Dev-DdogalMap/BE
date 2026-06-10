package com.ddogalmap.domain.chat.dto.response;

import java.time.LocalDateTime;

public record DirectChatRoomResponse(
        Long directChatRoomId,
        Long targetUserId,
        String targetNickname,
        String targetProfileImageUrl,
        Integer targetLevel,
        String targetLevelName,
        String targetSpecialty,
        Boolean targetCertified,
        String lastMessage,
        LocalDateTime lastMessageAt,
        Long lastMessageSenderId,
        Integer unreadCount,
        LocalDateTime createdAt
) {
}
