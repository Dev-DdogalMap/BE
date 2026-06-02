package com.ddogalmap.domain.chat.dto.request;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;

public record DirectChatMessageRequest(
        ChatMessageType messageType,
        String message
) {
}
