package com.ddogalmap.domain.chat.dto.request;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;

public record DirectChatWebSocketMessageRequest(
        ChatMessageType messageType,
        String message
) {
}
