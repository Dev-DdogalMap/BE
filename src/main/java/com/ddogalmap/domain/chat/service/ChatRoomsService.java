package com.ddogalmap.domain.chat.service;

import com.ddogalmap.domain.chat.dto.groupChat.request.CreateChatRoomRequest;
import com.ddogalmap.domain.chat.dto.groupChat.response.*;
import com.ddogalmap.domain.chat.dto.request.ChatMessageSendRequest;
import com.ddogalmap.domain.chat.entity.ChatMessages;
import com.ddogalmap.domain.chat.entity.ChatRoomMembers;
import com.ddogalmap.domain.chat.entity.ChatRooms;
import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;
import com.ddogalmap.domain.chat.enumtype.Status;
import com.ddogalmap.domain.chat.repository.ChatMessageRepository;
import com.ddogalmap.domain.chat.repository.ChatRoomMembersRepository;
import com.ddogalmap.domain.chat.repository.ChatRoomsRepository;
import com.ddogalmap.domain.foodtypes.entity.FoodType;
import com.ddogalmap.domain.foodtypes.repository.FoodTypeRepository;
import com.ddogalmap.domain.levels.dto.LevelExpEvent;
import com.ddogalmap.domain.levels.enumtype.ActivityType;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ChatRoomsService {
    private static final int DEFAULT_MESSAGE_PAGE_SIZE = 50;
    private static final String DEFAULT_IMAGE = "chat/default_image.png";

    private final ChatRoomsRepository chatRoomsRepository;
    private final ChatRoomMembersRepository chatRoomMembersRepository;
    private final UserRepository userRepository;
    private final FoodTypeRepository foodTypeRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ImageUtilService imageUtilService;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 그룹 채팅방 생성
     */
    @Transactional
    public CreateChatRoomResponse createChatRoom(Long ownerId, CreateChatRoomRequest request) {
        FoodType foodType = foodTypeRepository.findById(request.foodTypeId()).orElseThrow(() -> new IllegalArgumentException("음식 카테고리가 존재하지 않습니다."));

        //그룹 채팅방 생성
        ChatRooms room = chatRoomsRepository.save(ChatRooms.builder()
                .roomName(request.roomName())
                .foodType(foodType)
                .region(request.region())
                .participantCount(1)
                .maxParticipantCount(request.maxParticipantCount())
                .imageUrl(request.imageKey() == null? DEFAULT_IMAGE : request.imageKey())
                .build());

        //멤버 추가
        User user = userRepository.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        chatRoomMembersRepository.save(ChatRoomMembers.builder()
                .chatRoom(room)
                .role(ChatRoomMemberRole.OWNER)
                .user(user)
                .build());

        return new CreateChatRoomResponse(room.getId());
    }

    /**
     * 그룹 채팅방 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(Long userId, Long roomId, Integer size) {
        ChatRooms room = getParticipatingRoom(userId, roomId);
        int pageSize = size == null || size < 1 ? DEFAULT_MESSAGE_PAGE_SIZE : Math.min(size, 100);

        return chatMessageRepository.findGroupRecentMessages(
                        room.getId(),
                        PageRequest.of(0, pageSize)
                ).stream()
                .sorted(Comparator.comparing(ChatMessages::getCreatedAt)
                        .thenComparing(ChatMessages::getChatMessageId))
                .map(chatMessages -> {
                    User user = chatMessages.getWriter();

                    return new ChatMessageResponse(
                        chatMessages.getChatMessageId(),
                        room.getId(),
                        user.getUserId(),
                        user.getNickname(),
                        user.getProfileImageUrl(),
                        12,  //이거 레벨 조회해서 넣어야함
                        chatMessages.getStatus(),
                        chatMessages.getMessage(),
                        chatMessages.getCreatedAt());
                })
                .toList();
    }

    /**
     * 그룹 채팅방 참여
     */
    @Transactional
    public JoinChatRoomResponse joinChatRoom(Long userId, Long roomId) {
        User user = userRepository.getReferenceById(userId);  //FK 연결만 하면 되서 프록시 객체만 필요
        ChatRooms chatRoom = chatRoomsRepository.findById(roomId).orElseThrow();

        //중복 참여 검증
        if (chatRoomMembersRepository.existsByChatRoom_idAndUser_UserId(roomId, userId)) {
            return new JoinChatRoomResponse(chatRoom.getId(), true);
        }

        //채팅방 full 검증
        if(chatRoom.getParticipantCount() >= chatRoom.getMaxParticipantCount()) {
            throw new IllegalArgumentException("해당 그룹 채팅방은 인원이 다 찼습니다.");
        }

        //그룹 채팅 참여자 저장
        chatRoomMembersRepository.save(ChatRoomMembers.builder()
                        .user(user)
                        .chatRoom(chatRoom)
                        .role(ChatRoomMemberRole.MEMBER)
                .build());

        //그룹 채팅 참여인원 수정 - 동시성 문제를 막기 위해 원자적 update
        chatRoomsRepository.increaseParticipantCount(chatRoom.getId());

        eventPublisher.publishEvent(new LevelExpEvent(userId, ActivityType.GROUP_CHAT_JOIN, roomId));

        return new JoinChatRoomResponse(chatRoom.getId(), false);
    }

    /**
     * 그룹 채팅 메세지 저장
     */
    @Transactional
    public ChatMessageResponse saveChatMessage(Long senderId, ChatMessageSendRequest request) {
        if (request.roomId() == null) {
            throw new IllegalArgumentException("그룹 채팅방 ID는 필수입니다.");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }

        ChatRooms room = getParticipatingRoom(senderId, request.roomId());
        User writer = userRepository.getReferenceById(senderId);

        //메세지 저장
        ChatMessages message = chatMessageRepository.save(
                ChatMessages.builder()
                        .message(request.content().trim())
                        .writer(writer)
                        .chatRoom(room)
                        .status(Status.SENT)
                        .build()
        );
        return new ChatMessageResponse(
                message.getChatMessageId(),
                room.getId(), writer.getUserId(),
                writer.getNickname(),
                writer.getProfileImageUrl(),
                12,  //이거 레벨 조회해서 넣어야함
                message.getStatus(),
                message.getMessage(),
                message.getCreatedAt());
    }

    /**
     * 채팅방 정보 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomInfoResponse getChatRoomInfo(Long userId, Long roomId) {
        ChatRooms room = getParticipatingRoom(userId, roomId);
        List<MemberInfo> memberInfos = chatRoomMembersRepository.findAllByChatRoom(room);

        return new ChatRoomInfoResponse(
                imageUtilService.getImageUrl(room.getImageUrl()),
                room.getRoomName(),
                room.getParticipantCount(),
                room.getMaxParticipantCount(),
                room.getFoodType().getType(),
                room.getRegion(),
                memberInfos);
    }

    /**
     * 그룹 채팅방 전체 목록 조회
     */
    public ChatRoomListResponse getChatRoomList(Pageable pageable) {
        Slice<ChatRoomListThumbnailResponse> chatRoomSlice = chatRoomsRepository.findChatRoomListThumbnail(pageable);
        List<ChatRoomListThumbnailResponse> chatRoomList = chatRoomSlice.stream()
                .map(chatRoom ->
                        new ChatRoomListThumbnailResponse(
                        chatRoom.roomId(),
                        imageUtilService.getImageUrl(chatRoom.roomImageUrl()),
                        chatRoom.roomName(),
                        chatRoom.participantCount(),
                        chatRoom.maxParticipantCount(),
                        chatRoom.createdAt(),
                        chatRoom.latestMessageTime())).toList();
        return new ChatRoomListResponse(chatRoomSlice.hasNext(), chatRoomList);
    }

    //채팅방 조회
    private ChatRooms getParticipatingRoom(Long userId, Long roomId) {
        ChatRooms room = chatRoomsRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 채팅방이 존재하지 않습니다. id=" + roomId));

        if (!chatRoomMembersRepository.existsByChatRoom_idAndUser_UserId(roomId, userId)) {
            throw new IllegalArgumentException("참여하지 않은 그룹 채팅방에는 접근할 수 없습니다.");
        }
        return room;
    }
}
