package com.ddogalmap.domain.chat.dto.response;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.chat.enumtype.ChatRoomType;
import com.ddogalmap.domain.chat.enumtype.Status;

import java.time.LocalDateTime;

public record ChatMessageBroadcastResponse(
        ChatRoomType roomType,
        Long roomId,
        Long senderId,
        //ChatMessageType messageType,
        Status status,
        String content,
        LocalDateTime sentAt
) {
}
