package com.ddogalmap.domain.chat.dto.response;

import com.ddogalmap.domain.chat.enumtype.Status;

import java.time.LocalDateTime;

public record DirectChatMessageResponse(
        Long messageId,
        Long directChatRoomId,
        Long senderId,
        String senderNickname,
        Status status,
        String message,
        LocalDateTime createdAt
) {
}
