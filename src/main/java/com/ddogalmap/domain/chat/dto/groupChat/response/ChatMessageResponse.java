package com.ddogalmap.domain.chat.dto.groupChat.response;

import com.ddogalmap.domain.chat.enumtype.Status;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long chatMessageId,
        Long chatRoomId,
        Long senderId,
        String senderNickname,
        Status status,
        String content,
        LocalDateTime createdAt
) {
}
