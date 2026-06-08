package com.ddogalmap.domain.chat.service;

import com.ddogalmap.domain.badges.dto.ChatRequestReceivedEvent;
import com.ddogalmap.domain.chat.dto.request.ChatMessageSendRequest;
import com.ddogalmap.domain.chat.dto.request.CreateDirectChatRoomRequest;
import com.ddogalmap.domain.chat.dto.response.DirectChatMessageResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatRoomResponse;
import com.ddogalmap.domain.chat.dto.response.MyChatRoomResponse;
import com.ddogalmap.domain.chat.entity.ChatMessages;
import com.ddogalmap.domain.chat.entity.DirectChatRoom;
import com.ddogalmap.domain.chat.enumtype.ChatRoomType;
import com.ddogalmap.domain.chat.enumtype.Status;
import com.ddogalmap.domain.chat.exception.DirectChatRoomNotFoundException;
import com.ddogalmap.domain.chat.exception.InvalidDirectChatRequestException;
import com.ddogalmap.domain.chat.exception.MessageBlankException;
import com.ddogalmap.domain.chat.exception.NotChatRoomMemberException;
import com.ddogalmap.domain.chat.mapper.DirectChatMapper;
import com.ddogalmap.domain.chat.repository.ChatMessageRepository;
import com.ddogalmap.domain.chat.repository.ChatRoomsRepository;
import com.ddogalmap.domain.chat.repository.DirectChatRoomRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.exception.UserNotFoundException;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DirectChatRoomService {

    private static final int DEFAULT_MESSAGE_PAGE_SIZE = 50;

    private final DirectChatRoomRepository directChatRoomRepository;
    private final ChatMessageRepository directChatMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomsRepository chatRoomsRepository;
    private final ImageUtilService imageUtilService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DirectChatRoomResponse createOrGetDirectChatRoom(Long requesterId, CreateDirectChatRoomRequest request) {
        Long targetUserId = request.targetUserId();
        if (requesterId.equals(targetUserId)) {
            throw new InvalidDirectChatRequestException("자기 자신과는 개인 채팅방을 만들 수 없습니다.");
        }

        User requester = getUser(requesterId);
        User receiver = getUser(targetUserId);

        Optional<DirectChatRoom> optionalDirectChatRoom = directChatRoomRepository.findBetweenUsers(requesterId, targetUserId);

        boolean isNewRoom = optionalDirectChatRoom.isEmpty();

        DirectChatRoom room = optionalDirectChatRoom
                .orElseGet(() -> directChatRoomRepository.save(DirectChatRoom.create(requester, receiver)));

        room.restore(requesterId);

        if(isNewRoom) {
            eventPublisher.publishEvent(new ChatRequestReceivedEvent(receiver.getUserId()));
        }

        ChatMessages latestMessage = directChatMessageRepository
                .findTopByDirectChatRoom_DirectChatRoomIdOrderByCreatedAtDescChatMessageIdDesc(room.getDirectChatRoomId())
                .orElse(null);

        return DirectChatMapper.toRoomResponse(
                room,
                requesterId,
                latestMessage == null ? null : latestMessage.getMessage(),
                latestMessage == null ? null : latestMessage.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<MyChatRoomResponse> getMyChatRooms(Long currentUserId) {
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));

        // 1:1 채팅 목록 조회
        List<MyChatRoomResponse> chatList = directChatRoomRepository.findAllByParticipantWithLatestMessage(currentUserId);

        // 그룹 채팅 목록 조회
        List<MyChatRoomResponse> groupChatList = chatRoomsRepository.findMyChatRooms(user).stream()
                .map(chatRoom -> new MyChatRoomResponse(
                        chatRoom.directChatRoomId(),
                        chatRoom.targetUserId(),
                        chatRoom.targetNickname(),
                        imageUtilService.getImageUrl(chatRoom.targetProfileImageUrl()),  //cdn url 적용
                        chatRoom.lastMessage(),
                        chatRoom.lastMessageAt(),
                        chatRoom.unreadCount(),
                        chatRoom.createdAt(),
                        "GROUP"
                ))
                .toList();
        chatList.addAll(groupChatList);
        return chatList;
    }

    @Transactional(readOnly = true)
    public List<DirectChatMessageResponse> getDirectChatMessages(Long currentUserId, Long directChatRoomId, Integer size) {
        DirectChatRoom room = getVisibleRoom(currentUserId, directChatRoomId);
        int pageSize = size == null || size < 1 ? DEFAULT_MESSAGE_PAGE_SIZE : Math.min(size, 100);

        return directChatMessageRepository.findRecentMessages(
                        room.getDirectChatRoomId(),
                        PageRequest.of(0, pageSize)
                ).stream()
                .sorted(Comparator.comparing(ChatMessages::getCreatedAt)
                        .thenComparing(ChatMessages::getChatMessageId))
                .map(DirectChatMapper::toMessageResponse)
                .toList();
    }

    @Transactional
    public DirectChatMessageResponse saveDirectChatMessage(Long senderId, ChatMessageSendRequest request) {
        if (request.roomType() != ChatRoomType.DIRECT) {
            throw new InvalidDirectChatRequestException("개인 채팅 메시지만 저장할 수 있습니다.");
        }
        if (request.roomId() == null) {
            throw new InvalidDirectChatRequestException("개인 채팅방 ID는 필수입니다.");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new MessageBlankException("메시지 내용은 비어 있을 수 없습니다.");
        }

        DirectChatRoom room = getVisibleRoom(senderId, request.roomId());
        User sender = getUser(senderId);
        ChatMessages message = directChatMessageRepository.save(
                ChatMessages.create(
                        room,
                        sender,
                        Status.SENT,
                        request.content().trim()
                )
        );

        return DirectChatMapper.toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public DirectChatRoomResponse getDirectChatRoom(Long currentUserId, Long directChatRoomId) {
        DirectChatRoom room = getVisibleRoom(currentUserId, directChatRoomId);
        ChatMessages latestMessage = directChatMessageRepository
                .findTopByDirectChatRoom_DirectChatRoomIdOrderByCreatedAtDescChatMessageIdDesc(room.getDirectChatRoomId())
                .orElse(null);

        return DirectChatMapper.toRoomResponse(
                room,
                currentUserId,
                latestMessage == null ? null : latestMessage.getMessage(),
                latestMessage == null ? null : latestMessage.getCreatedAt()
        );
    }

    @Transactional
    public void leaveDirectChatRoom(Long currentUserId, Long directChatRoomId) {
        DirectChatRoom room = getParticipatingRoom(currentUserId, directChatRoomId);
        if (room.isDeleted()) {
            throw new DirectChatRoomNotFoundException("개인 채팅방이 존재하지 않습니다. id=" + directChatRoomId);
        }
        room.leave(currentUserId);
    }

    private DirectChatRoom getVisibleRoom(Long userId, Long directChatRoomId) {
        DirectChatRoom room = getParticipatingRoom(userId, directChatRoomId);
        if (!room.isVisibleTo(userId)) {
            throw new NotChatRoomMemberException("나간 개인 채팅방에는 접근할 수 없습니다.");
        }
        return room;
    }

    private DirectChatRoom getParticipatingRoom(Long userId, Long directChatRoomId) {
        DirectChatRoom room = directChatRoomRepository.findById(directChatRoomId)
                .orElseThrow(() -> new DirectChatRoomNotFoundException("개인 채팅방이 존재하지 않습니다. id=" + directChatRoomId));

        if (!room.hasParticipant(userId)) {
            throw new NotChatRoomMemberException("참여하지 않은 개인 채팅방에는 접근할 수 없습니다.");
        }
        return room;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자가 존재하지 않습니다. id=" + userId));
    }
}
