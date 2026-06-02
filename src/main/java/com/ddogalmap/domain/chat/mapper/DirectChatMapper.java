package com.ddogalmap.domain.chat.mapper;

import com.ddogalmap.domain.chat.dto.response.DirectChatMessageResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatRoomResponse;
import com.ddogalmap.domain.chat.entity.ChatMessages;
import com.ddogalmap.domain.chat.entity.DirectChatRoom;
import com.ddogalmap.domain.chat.enumtype.Status;
import com.ddogalmap.domain.users.entity.User;

import java.time.LocalDateTime;

public final class DirectChatMapper {

    private DirectChatMapper() {
    }

    public static DirectChatRoomResponse toRoomResponse(
            DirectChatRoom room,
            Long currentUserId,
            String lastMessage,
            LocalDateTime lastMessageAt
    ) {
        User opponent = room.getOpponent(currentUserId);
        return new DirectChatRoomResponse(
                room.getDirectChatRoomId(),
                opponent.getUserId(),
                opponent.getNickname(),
                opponent.getProfileImageUrl(),
                lastMessage,
                lastMessageAt,
                0,
                room.getCreatedAt()
        );
    }

    public static DirectChatMessageResponse toMessageResponse(ChatMessages message) {
        return new DirectChatMessageResponse(
                message.getChatMessageId(),
                message.getDirectChatRoom().getDirectChatRoomId(),
                message.getSender().getUserId(),
                message.getSender().getNickname(),
                message.getMessageType(),
                Status.SENT,
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
