package com.ddogalmap.domain.chat.dto.groupChat.response;

import java.util.List;

public record ChatRoomListResponse(
        Boolean hasNext,
        List<ChatRoomListThumbnailResponse> chatRoomList
) {
}
