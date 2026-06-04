package com.ddogalmap.domain.chat.dto.groupChat.response;

public record JoinChatRoomResponse(
        Long chatRoomId,
        Boolean isMember
) {
}
