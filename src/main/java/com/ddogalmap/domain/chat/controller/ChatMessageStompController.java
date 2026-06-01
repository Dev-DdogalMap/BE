package com.ddogalmap.domain.chat.controller;

import com.ddogalmap.domain.chat.dto.groupChat.response.ChatMessageResponse;
import com.ddogalmap.domain.chat.dto.request.ChatMessageSendRequest;
import com.ddogalmap.domain.chat.dto.response.ChatMessageBroadcastResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatMessageResponse;
import com.ddogalmap.domain.chat.service.ChatRoomsService;
import com.ddogalmap.domain.chat.service.DirectChatRoomService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageStompController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final DirectChatRoomService directChatRoomService;
    private final ChatRoomsService chatRoomsService;

    @MessageMapping("/chats/messages")
    public void sendMessage(
            ChatMessageSendRequest request,
            UsernamePasswordAuthenticationToken authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        DirectChatMessageResponse savedMessage = directChatRoomService.saveDirectChatMessage(principal.userId(), request);

        ChatMessageBroadcastResponse response = new ChatMessageBroadcastResponse(
                request.roomType(),
                savedMessage.directChatRoomId(),
                savedMessage.senderId(),
                //savedMessage.messageType(),
                savedMessage.status(),
                savedMessage.content(),
                savedMessage.createdAt()
        );

        simpMessagingTemplate.convertAndSend("/sub/chats/direct/" + savedMessage.directChatRoomId(), response);
    }

    @MessageMapping("/chats/group/messages")
    public void sendGroupMessage(
            ChatMessageSendRequest request,
            UsernamePasswordAuthenticationToken authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ChatMessageResponse savedMessage = chatRoomsService.saveChatMessage(principal.userId(), request);

        ChatMessageBroadcastResponse response = new ChatMessageBroadcastResponse(
                request.roomType(),
                savedMessage.chatRoomId(),
                savedMessage.senderId(),
                savedMessage.status(),
                savedMessage.content(),
                savedMessage.createdAt()
        );

        simpMessagingTemplate.convertAndSend("/sub/chats/group/" + savedMessage.chatRoomId(), response);
    }
}
