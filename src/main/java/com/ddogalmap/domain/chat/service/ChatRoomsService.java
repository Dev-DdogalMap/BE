package com.ddogalmap.domain.chat.service;

import com.ddogalmap.domain.chat.dto.groupChat.request.CreateChatRoomRequest;
import com.ddogalmap.domain.chat.dto.groupChat.response.CreateChatRoomResponse;
import com.ddogalmap.domain.chat.entity.ChatRoomMembers;
import com.ddogalmap.domain.chat.entity.ChatRooms;
import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;
import com.ddogalmap.domain.chat.repository.ChatRoomMembersRepository;
import com.ddogalmap.domain.chat.repository.ChatRoomsRepository;
import com.ddogalmap.domain.foodtypes.entity.FoodType;
import com.ddogalmap.domain.foodtypes.repository.FoodTypeRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ChatRoomsService {

    private final ChatRoomsRepository chatRoomsRepository;
    private final ChatRoomMembersRepository chatRoomMembersRepository;
    private final UserRepository userRepository;
    private final FoodTypeRepository foodTypeRepository;

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
}
