package com.ddogalmap.domain.chat.dto.request;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.chat.enumtype.ChatRoomType;
import com.ddogalmap.domain.chat.enumtype.Status;

public record ChatMessageSendRequest(
        ChatRoomType roomType,
        Long roomId,
        //ChatMessageType messageType,
        Status status,
        String content
) {
}
