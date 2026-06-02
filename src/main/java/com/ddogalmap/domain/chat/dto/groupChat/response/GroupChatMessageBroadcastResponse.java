package com.ddogalmap.domain.chat.dto.groupChat.response;

import com.ddogalmap.domain.chat.enumtype.ChatRoomType;
import com.ddogalmap.domain.chat.enumtype.Status;

import java.time.LocalDateTime;

public record GroupChatMessageBroadcastResponse(
        ChatRoomType roomType,
        Long roomId,
        Long senderId,
        String senderNickname,
        String senderProfileImage,
        Integer senderLevel,
        Status status,
        String content,
        LocalDateTime sentAt
) {
}
