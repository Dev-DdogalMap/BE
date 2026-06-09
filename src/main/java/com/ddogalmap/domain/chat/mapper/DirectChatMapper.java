package com.ddogalmap.domain.chat.mapper;

import com.ddogalmap.domain.chat.dto.response.DirectChatMessageResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatRoomResponse;
import com.ddogalmap.domain.chat.entity.ChatMessages;
import com.ddogalmap.domain.chat.entity.DirectChatRoom;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.enumtype.UserStatus;

import java.time.LocalDateTime;

public final class DirectChatMapper {

    private DirectChatMapper() {
    }

    public static DirectChatRoomResponse toRoomResponse(
            DirectChatRoom room,
            Long currentUserId,
            String lastMessage,
            LocalDateTime lastMessageAt,
            Integer targetLevel,
            String targetLevelName,
            String targetSpecialty,
            Boolean targetCertified
    ) {
        User opponent = room.getOpponent(currentUserId);
        boolean opponentUnavailable = room.hasOpponentLeft(currentUserId)
                || opponent.getStatus() == UserStatus.DELETED;
        return new DirectChatRoomResponse(
                room.getDirectChatRoomId(),
                opponent.getUserId(),
                opponentUnavailable ? "대화 상대 없음" : opponent.getNickname(),
                opponentUnavailable ? null : opponent.getProfileImageUrl(),
                opponentUnavailable ? null : targetLevel,
                opponentUnavailable ? null : targetLevelName,
                opponentUnavailable ? null : targetSpecialty,
                opponentUnavailable ? false : targetCertified,
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
                message.getWriter().getUserId(),
                message.getWriter().getNickname(),
                message.getStatus(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}
