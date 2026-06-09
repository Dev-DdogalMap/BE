package com.ddogalmap.domain.chat.service;

import com.ddogalmap.domain.chat.dto.groupChat.request.UpdateChatRoomRequest;
import com.ddogalmap.domain.chat.entity.ChatRooms;
import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;
import com.ddogalmap.domain.chat.repository.ChatRoomMembersRepository;
import com.ddogalmap.domain.chat.repository.ChatRoomsRepository;
import com.ddogalmap.domain.foodtypes.entity.FoodType;
import com.ddogalmap.domain.foodtypes.repository.FoodTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 트랜잭션 분리를 위한 service
 */
@Service
@RequiredArgsConstructor
public class ChatRoomsTxService {
    private static final String DEFAULT_IMAGE = "chat/default_image.png";
    private final ChatRoomMembersRepository chatRoomMembersRepository;
    private final FoodTypeRepository foodTypeRepository;
    private final ChatRoomsRepository chatRoomsRepository;

    @Transactional
    public String updateTxChatRooms(Long roomId, Long ownerId, UpdateChatRoomRequest request) {
        //owner 검증
        if (!chatRoomMembersRepository.existsByChatRoom_idAndUser_UserIdAndRole(roomId, ownerId, ChatRoomMemberRole.OWNER)) {
            throw new IllegalArgumentException("그룹 채팅방 수정은 OWNER 권한만 가능합니다.");
        }
        FoodType foodType = foodTypeRepository.findById(request.foodTypeId()).orElseThrow(() -> new IllegalArgumentException("음식 카테고리가 존재하지 않습니다."));

        //그룹 채팅방 수정
        ChatRooms room = chatRoomsRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("해당 그룹 채팅방이 존재하지 않습니다."));
        if (room.getParticipantCount() > request.maxParticipantCount()) {  //수정할 최대인원이 현재인원보다 적을 경우 에러
            throw new IllegalArgumentException("최대 인원이 현재 인원보다 적습니다.");
        }
        String oldImageKey = room.getImageUrl();  //기존키 꺼내기
        room.updateChatRoom(request, request.imageKey(), foodType);
        return request.imageKey() == null? null : oldImageKey;  //사진 수정 안하는 경우엔 old키로 삭제하면 안됨
    }
}
