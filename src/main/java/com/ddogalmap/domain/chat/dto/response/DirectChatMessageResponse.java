package com.ddogalmap.domain.chat.dto.response;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.chat.enumtype.Status;

import java.time.LocalDateTime;

public record DirectChatMessageResponse(
        Long directChatMessageId,
        Long directChatRoomId,
        Long senderId,
        String senderNickname,
        //ChatMessageType messageType,
        Status status,
        String content,
        LocalDateTime createdAt
) {
}
