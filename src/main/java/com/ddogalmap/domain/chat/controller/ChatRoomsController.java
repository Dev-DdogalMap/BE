package com.ddogalmap.domain.chat.controller;

import com.ddogalmap.domain.chat.dto.groupChat.image.UrlDto;
import com.ddogalmap.domain.chat.dto.groupChat.request.CreateChatRoomRequest;
import com.ddogalmap.domain.chat.dto.groupChat.request.UpdateChatRoomRequest;
import com.ddogalmap.domain.chat.dto.groupChat.response.*;
import com.ddogalmap.domain.chat.service.ChatRoomsService;
import com.ddogalmap.domain.chat.service.ImageUtilService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(name = "ChatRooms", description = "그룹 채팅방 및 메시지 API")
@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomsController {

    private static final String S3_FOLDER = "chat";

    private final ChatRoomsService chatRoomsService;
    private final ImageUtilService imageUtilService;

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
            summary = "그룹 채팅방 이미지 업로드용 presigned url 발급",
            description = "그룹 채팅방 이미지 업로드용 presigned url 발급합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/presigned-url")
    public UrlDto getPresignedUrl(@RequestParam("imageFileName") String imageFileName) {
        return imageUtilService.generatePresignedUrl(S3_FOLDER, imageFileName);
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

    @Operation(
            summary = "그룹 채팅방 정보 조회",
            description = "그룹 채팅방의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{roomId}")
    public ChatRoomInfoResponse getChatRoomInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId
    ) {
        return chatRoomsService.getChatRoomInfo(principal.userId(), roomId);
    }

    @Operation(
            summary = "토큰 반환",
            description = "웹소켓 연결에 쿠키에 담긴 토큰은 사용 못해서 따로 토큰 반환 요청 필요",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/auth/token")
    public ResponseEntity<Map<String, String>> getToken(HttpServletRequest request) {
        String token = Arrays.stream(request.getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("토큰이 없습니다."));

        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    @Operation(
            summary = "그룹 채팅방 전체 목록 조회",
            description = "그룹 채팅방의 전체 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ChatRoomListResponse getChatRoomList(
            @PageableDefault(size = 20)  //20개씩 조회 기본값. sort는 쿼리에서 고정(클라이언트에서 지정불가)
            Pageable pageable
    ) {
        return chatRoomsService.getChatRoomList(pageable);
    }

    @Operation(
            summary = "그룹 채팅방 수정",
            description = "그룹 채팅방 owner가 정보를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/{roomId}")
    public UpdateChatRoomResponse updateChatRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId,
            @RequestBody UpdateChatRoomRequest request) {
        return chatRoomsService.updateChatRoom(principal.userId(), roomId, request);
    }

    @Operation(
            summary = "그룹 채팅방 멤버 목록 조회",
            description = "그룹 채팅방에 참여한 멤버 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{roomId}/members")
    public ChatRoomMembersResponse getChatRoomMembers(
            @AuthenticationPrincipal UserPrincipal principal, //방 참여자 여부 검증 필요
            @PathVariable Long roomId) {
        return chatRoomsService.getChatRoomMembers(principal.userId(), roomId);
    }

    @Operation(
            summary = "그룹 채팅방 나가기",
            description = "그룹 채팅방에 참여한 사람이 방에서 나갑니다. OWNER가 1명 뿐일 경우, OWNER는 나갈 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{roomId}")
    public LeaveChatRoomResponse leaveChatRoom(
            @AuthenticationPrincipal UserPrincipal principal, //방 참여자 여부 검증 필요
            @PathVariable Long roomId) {
        return chatRoomsService.leaveChatRoom(principal.userId(), roomId);
    }
}
