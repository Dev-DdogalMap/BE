package com.ddogalmap.domain.chat.controller;

import com.ddogalmap.domain.chat.dto.groupChat.request.CreateChatRoomRequest;
import com.ddogalmap.domain.chat.dto.groupChat.response.CreateChatRoomResponse;
import com.ddogalmap.domain.chat.dto.request.CreateDirectChatRoomRequest;
import com.ddogalmap.domain.chat.dto.response.DirectChatRoomResponse;
import com.ddogalmap.domain.chat.service.ChatRoomsService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Direct Chat", description = "1:1 채팅방 및 메시지 API")
@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomsController {

    private final ChatRoomsService chatRoomsService;

    @Operation(
            summary = "그룹 채팅방 생성",
            description = "그룹 채팅방을 새로 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public CreateChatRoomResponse createChatRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateChatRoomRequest request
    ) {
        return chatRoomsService.createChatRoom(principal.userId(), request);
    }
}
