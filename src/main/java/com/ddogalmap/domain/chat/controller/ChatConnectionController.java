package com.ddogalmap.domain.chat.controller;

import com.ddogalmap.domain.chat.dto.response.WebSocketConnectionInfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatConnectionController {

    @GetMapping("/ws-info")
    public WebSocketConnectionInfoResponse getConnectionInfo() {
        return new WebSocketConnectionInfoResponse(
                "/ws-chat",
                "/pub/chats/messages",
                List.of(
                        "/sub/chats/direct/{directChatRoomId}",
                        "/sub/chats/group/{chatRoomId}"
                ),
                "Authorization: Bearer {accessToken}"
        );
    }
}
