package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.chat.dto.response.DirectChatRoomEventResponse;
import com.ddogalmap.domain.chat.entity.DirectChatRoom;
import com.ddogalmap.domain.chat.repository.DirectChatRoomRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.global.infrastructure.KakaoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWithdrawalService {

    private final UserRepository userRepository;
    private final KakaoClient kakaoClient;
    private final DirectChatRoomRepository directChatRoomRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Long kakaoId = user.getKakaoId();

        if (kakaoId != null) {
            kakaoClient.unlink(kakaoId);
        }

        user.withdraw();
        leaveDirectChatRooms(userId);
    }

    private void leaveDirectChatRooms(Long userId) {
        List<DirectChatRoom> rooms = directChatRoomRepository.findActiveRoomsByParticipant(userId);

        for (DirectChatRoom room : rooms) {
            room.leave(userId);
            simpMessagingTemplate.convertAndSend(
                    "/topic/direct-chats/" + room.getDirectChatRoomId(),
                    DirectChatRoomEventResponse.left(room.getDirectChatRoomId(), userId)
            );
        }
    }
}
