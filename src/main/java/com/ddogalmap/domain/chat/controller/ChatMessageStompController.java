package com.ddogalmap.domain.chat.controller;

import com.ddogalmap.domain.chat.dto.request.ChatMessageSendRequest;
import com.ddogalmap.domain.chat.dto.request.DirectChatWebSocketMessageRequest;
import com.ddogalmap.domain.chat.dto.response.ChatMessageBroadcastResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatMessageResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatWebSocketMessageResponse;
import com.ddogalmap.domain.chat.enumtype.ChatRoomType;
import com.ddogalmap.domain.chat.service.DirectChatRoomService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageStompController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final DirectChatRoomService directChatRoomService;

    @MessageMapping("/chats/messages")
    public void sendMessage(
            ChatMessageSendRequest request,
            UsernamePasswordAuthenticationToken authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        DirectChatMessageResponse savedMessage = directChatRoomService.saveDirectChatMessage(principal.userId(), request);

        broadcast(savedMessage);
    }

    @MessageMapping("/direct-chats/{directChatRoomId}/messages")
    public void sendDirectChatMessage(
            @DestinationVariable Long directChatRoomId,
            DirectChatWebSocketMessageRequest request,
            UsernamePasswordAuthenticationToken authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        DirectChatMessageResponse savedMessage = directChatRoomService.saveDirectChatMessage(
                principal.userId(),
                new ChatMessageSendRequest(
                        ChatRoomType.DIRECT,
                        directChatRoomId,
                        request.messageType(),
                        request.message()
                )
        );
        broadcast(savedMessage);
    }

    private void broadcast(DirectChatMessageResponse savedMessage) {
        ChatMessageBroadcastResponse legacyResponse = new ChatMessageBroadcastResponse(
                savedMessage.messageId(),
                savedMessage.directChatRoomId(),
                savedMessage.senderId(),
                savedMessage.senderNickname(),
                savedMessage.messageType(),
                savedMessage.status(),
                savedMessage.message(),
                savedMessage.createdAt()
        );

        DirectChatWebSocketMessageResponse response = new DirectChatWebSocketMessageResponse(
                savedMessage.messageId(),
                savedMessage.directChatRoomId(),
                savedMessage.senderId(),
                savedMessage.senderNickname(),
                savedMessage.messageType(),
                savedMessage.status(),
                savedMessage.message(),
                savedMessage.createdAt()
        );

        simpMessagingTemplate.convertAndSend("/sub/chats/direct/" + savedMessage.directChatRoomId(), legacyResponse);
        simpMessagingTemplate.convertAndSend("/topic/direct-chats/" + savedMessage.directChatRoomId(), response);
    }
}
