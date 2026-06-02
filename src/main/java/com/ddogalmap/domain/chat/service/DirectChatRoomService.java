package com.ddogalmap.domain.chat.service;

import com.ddogalmap.domain.chat.dto.request.ChatMessageSendRequest;
import com.ddogalmap.domain.chat.dto.request.CreateDirectChatRoomRequest;
import com.ddogalmap.domain.chat.dto.response.DirectChatMessageResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatRoomResponse;
import com.ddogalmap.domain.chat.entity.ChatMessages;
import com.ddogalmap.domain.chat.entity.DirectChatRoom;
import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.chat.enumtype.ChatRoomType;
import com.ddogalmap.domain.chat.exception.DirectChatRoomNotFoundException;
import com.ddogalmap.domain.chat.exception.InvalidDirectChatRequestException;
import com.ddogalmap.domain.chat.exception.MessageBlankException;
import com.ddogalmap.domain.chat.exception.NotChatRoomMemberException;
import com.ddogalmap.domain.chat.mapper.DirectChatMapper;
import com.ddogalmap.domain.chat.repository.DirectChatMessageRepository;
import com.ddogalmap.domain.chat.repository.DirectChatRoomRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.exception.UserNotFoundException;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectChatRoomService {

    private static final int DEFAULT_MESSAGE_PAGE_SIZE = 50;

    private final DirectChatRoomRepository directChatRoomRepository;
    private final DirectChatMessageRepository directChatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public DirectChatRoomResponse createOrGetDirectChatRoom(Long requesterId, CreateDirectChatRoomRequest request) {
        Long targetUserId = request.targetUserId();
        if (requesterId.equals(targetUserId)) {
            throw new InvalidDirectChatRequestException("자기 자신과는 개인 채팅방을 만들 수 없습니다.");
        }

        User requester = getUser(requesterId);
        User receiver = getUser(targetUserId);

        DirectChatRoom room = directChatRoomRepository.findBetweenUsers(requesterId, targetUserId)
                .orElseGet(() -> directChatRoomRepository.save(DirectChatRoom.create(requester, receiver)));

        ChatMessages latestMessage = directChatMessageRepository
                .findTopByDirectChatRoom_DirectChatRoomIdOrderByCreatedAtDescChatMessageIdDesc(room.getDirectChatRoomId())
                .orElse(null);

        return DirectChatMapper.toRoomResponse(
                room,
                requesterId,
                latestMessage == null ? null : latestMessage.getContent(),
                latestMessage == null ? null : latestMessage.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<DirectChatRoomResponse> getMyDirectChatRooms(Long currentUserId) {
        return directChatRoomRepository.findAllByParticipant(currentUserId).stream()
                .map(room -> {
                    ChatMessages latestMessage = directChatMessageRepository
                            .findTopByDirectChatRoom_DirectChatRoomIdOrderByCreatedAtDescChatMessageIdDesc(room.getDirectChatRoomId())
                            .orElse(null);

                    return DirectChatMapper.toRoomResponse(
                            room,
                            currentUserId,
                            latestMessage == null ? null : latestMessage.getContent(),
                            latestMessage == null ? null : latestMessage.getCreatedAt()
                    );
                })
                .sorted(Comparator.comparing(
                        DirectChatRoomResponse::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ).thenComparing(DirectChatRoomResponse::directChatRoomId, Comparator.reverseOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DirectChatMessageResponse> getDirectChatMessages(Long currentUserId, Long directChatRoomId, Integer size) {
        DirectChatRoom room = getParticipatingRoom(currentUserId, directChatRoomId);
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

        DirectChatRoom room = getParticipatingRoom(senderId, request.roomId());
        User sender = getUser(senderId);
        ChatMessageType messageType = request.messageType() == null ? ChatMessageType.TEXT : request.messageType();

        ChatMessages message = directChatMessageRepository.save(
                ChatMessages.create(
                        room,
                        sender,
                        messageType,
                        request.content().trim()
                )
        );

        return DirectChatMapper.toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public DirectChatRoomResponse getDirectChatRoom(Long currentUserId, Long directChatRoomId) {
        DirectChatRoom room = getParticipatingRoom(currentUserId, directChatRoomId);
        ChatMessages latestMessage = directChatMessageRepository
                .findTopByDirectChatRoom_DirectChatRoomIdOrderByCreatedAtDescChatMessageIdDesc(room.getDirectChatRoomId())
                .orElse(null);

        return DirectChatMapper.toRoomResponse(
                room,
                currentUserId,
                latestMessage == null ? null : latestMessage.getContent(),
                latestMessage == null ? null : latestMessage.getCreatedAt()
        );
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
