package com.ddogalmap.domain.chat.dto.response;

public record DirectChatRoomEventResponse(
        String eventType,
        Long directChatRoomId,
        Long userId
) {
    public static DirectChatRoomEventResponse left(Long directChatRoomId, Long userId) {
        return new DirectChatRoomEventResponse("DIRECT_CHAT_ROOM_LEFT", directChatRoomId, userId);
    }
}
