package com.ddogalmap.domain.chat.dto.response;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.chat.enumtype.ChatRoomType;

import java.time.LocalDateTime;

public record ChatMessageBroadcastResponse(
        ChatRoomType roomType,
        Long roomId,
        Long senderId,
        ChatMessageType messageType,
        String content,
        LocalDateTime sentAt
) {
}
