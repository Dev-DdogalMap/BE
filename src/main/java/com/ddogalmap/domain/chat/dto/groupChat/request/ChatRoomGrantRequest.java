package com.ddogalmap.domain.chat.dto.groupChat.request;

import java.util.List;

public record ChatRoomGrantRequest(
        List<Long> grantedUserIds
) {
}
