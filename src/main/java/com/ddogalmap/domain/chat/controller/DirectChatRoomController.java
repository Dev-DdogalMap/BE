package com.ddogalmap.domain.chat.controller;

import com.ddogalmap.domain.chat.dto.request.ChatMessageSendRequest;
import com.ddogalmap.domain.chat.dto.request.CreateDirectChatRoomRequest;
import com.ddogalmap.domain.chat.dto.request.DirectChatMessageRequest;
import com.ddogalmap.domain.chat.dto.response.DirectChatMessageResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatRoomResponse;
import com.ddogalmap.domain.chat.dto.response.MyChatRoomResponse;
import com.ddogalmap.domain.chat.enumtype.ChatRoomType;
import com.ddogalmap.domain.chat.service.DirectChatRoomService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Direct Chat", description = "1:1 채팅방 및 메시지 API")
@RestController
@RequestMapping({"/api/direct-chat-rooms", "/api/direct-chats"})
@RequiredArgsConstructor
public class DirectChatRoomController {

    private final DirectChatRoomService directChatRoomService;

    @Operation(
            summary = "개인 채팅방 생성 또는 조회",
            description = "상대 사용자와의 기존 1:1 채팅방이 있으면 반환하고, 없으면 새로 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public DirectChatRoomResponse createOrGetDirectChatRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateDirectChatRoomRequest request
    ) {
        return directChatRoomService.createOrGetDirectChatRoom(principal.userId(), request);
    }

    @Operation(
            summary = "내 대화 목록 조회",
            description = "현재 로그인한 사용자가 참여 중인 1:1 채팅방 목록과 그룹 채팅 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public List<MyChatRoomResponse> getMyChatRooms(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return directChatRoomService.getMyChatRooms(principal.userId());
    }

    @Operation(
            summary = "개인 채팅방 상세 조회",
            description = "현재 로그인한 사용자가 참여 중인 특정 1:1 채팅방 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{directChatRoomId}")
    public DirectChatRoomResponse getDirectChatRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long directChatRoomId
    ) {
        return directChatRoomService.getDirectChatRoom(principal.userId(), directChatRoomId);
    }

    @Operation(
            summary = "개인 채팅 메시지 조회",
            description = "현재 로그인한 사용자가 참여 중인 1:1 채팅방의 최근 메시지를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{directChatRoomId}/messages")
    public List<DirectChatMessageResponse> getDirectChatMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long directChatRoomId,
            @RequestParam(required = false) Integer size
    ) {
        return directChatRoomService.getDirectChatMessages(principal.userId(), directChatRoomId, size);
    }

    @Operation(
            summary = "개인 채팅 메시지 저장",
            description = "현재 로그인한 사용자가 참여 중인 1:1 채팅방에 메시지를 저장합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{directChatRoomId}/messages")
    public DirectChatMessageResponse saveDirectChatMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long directChatRoomId,
            @RequestBody DirectChatMessageRequest request
    ) {
        return directChatRoomService.saveDirectChatMessage(
                principal.userId(),
                new ChatMessageSendRequest(
                        ChatRoomType.DIRECT,
                        directChatRoomId,
                        request.message()
                )
        );
    }
}
