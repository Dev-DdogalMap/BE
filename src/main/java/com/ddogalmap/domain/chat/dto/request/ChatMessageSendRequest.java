package com.ddogalmap.domain.chat.dto.request;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.chat.enumtype.ChatRoomType;

public record ChatMessageSendRequest(
        ChatRoomType roomType,
        Long roomId,
        ChatMessageType messageType,
        String content
) {
}
