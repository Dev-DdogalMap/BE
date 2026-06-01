package com.ddogalmap.domain.chat.service;

import com.ddogalmap.domain.chat.dto.groupChat.request.CreateChatRoomRequest;
import com.ddogalmap.domain.chat.dto.groupChat.response.ChatMessageResponse;
import com.ddogalmap.domain.chat.dto.groupChat.response.CreateChatRoomResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatMessageResponse;
import com.ddogalmap.domain.chat.entity.ChatMessages;
import com.ddogalmap.domain.chat.entity.ChatRoomMembers;
import com.ddogalmap.domain.chat.entity.ChatRooms;
import com.ddogalmap.domain.chat.entity.DirectChatRoom;
import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;
import com.ddogalmap.domain.chat.mapper.DirectChatMapper;
import com.ddogalmap.domain.chat.repository.ChatMessageRepository;
import com.ddogalmap.domain.chat.repository.ChatRoomMembersRepository;
import com.ddogalmap.domain.chat.repository.ChatRoomsRepository;
import com.ddogalmap.domain.foodtypes.entity.FoodType;
import com.ddogalmap.domain.foodtypes.repository.FoodTypeRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ChatRoomsService {
    private static final int DEFAULT_MESSAGE_PAGE_SIZE = 50;

    private final ChatRoomsRepository chatRoomsRepository;
    private final ChatRoomMembersRepository chatRoomMembersRepository;
    private final UserRepository userRepository;
    private final FoodTypeRepository foodTypeRepository;
    private final ChatMessageRepository chatMessageRepository;

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
                        chatMessages.getStatus(),
                        chatMessages.getMessage(),
                        chatMessages.getCreatedAt());
                })
                .toList();
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
