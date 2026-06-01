package com.ddogalmap.domain.chat.dto.response;

import java.util.List;

public record WebSocketConnectionInfoResponse(
        String endpoint,
        String publishPrefix,
        List<String> subscribePatterns,
        String authorizationHeaderExample
) {
}
