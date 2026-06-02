package com.ddogalmap.domain.chat.dto.response;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.chat.enumtype.Status;
import java.time.LocalDateTime;

public record DirectChatWebSocketMessageResponse(
        Long messageId,
        Long directChatRoomId,
        Long senderId,
        String senderNickname,
        ChatMessageType messageType,
        Status status,
        String message,
        LocalDateTime createdAt
) {
}
