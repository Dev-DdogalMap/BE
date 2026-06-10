package com.ddogalmap.domain.chat.dto.groupChat.response;

import java.util.List;

public record ChatMessageCursorResponse(
        List<ChatMessageResponse> messages,
        Long nextCursor,    // 다음 요청에 쓸 커서
        boolean hasNext     // 다음 페이지 존재 여부
) {
}
