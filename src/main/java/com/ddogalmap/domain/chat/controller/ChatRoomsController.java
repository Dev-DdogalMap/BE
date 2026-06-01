package com.ddogalmap.domain.chat.controller;

import com.ddogalmap.domain.chat.dto.groupChat.request.CreateChatRoomRequest;
import com.ddogalmap.domain.chat.dto.groupChat.response.ChatMessageResponse;
import com.ddogalmap.domain.chat.dto.groupChat.response.CreateChatRoomResponse;
import com.ddogalmap.domain.chat.dto.groupChat.response.JoinChatRoomResponse;
import com.ddogalmap.domain.chat.service.ChatRoomsService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ChatRooms", description = "그룹 채팅방 및 메시지 API")
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

    @Operation(
            summary = "그룹 채팅 메시지 조회",
            description = "현재 로그인한 사용자가 참여 중인 그룹 채팅방의 최근 메시지를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{roomId}/messages")
    public List<ChatMessageResponse> getGroupChatMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId,
            @RequestParam(required = false) Integer size
    ) {
        return chatRoomsService.getChatMessages(principal.userId(), roomId, size);
    }

    @Operation(
            summary = "그룹 채팅방 참여",
            description = "그룹 채팅방에 참여합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{roomId}/join")
    public JoinChatRoomResponse joinChatRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId
    ) {
        return chatRoomsService.joinChatRoom(principal.userId(), roomId);
    }
}
