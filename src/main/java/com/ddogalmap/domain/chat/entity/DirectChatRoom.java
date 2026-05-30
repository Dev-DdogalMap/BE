package com.ddogalmap.domain.chat.entity;

import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "direct_chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long directChatRoomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    private DirectChatRoom(User requester, User receiver) {
        this.requester = requester;
        this.receiver = receiver;
    }

    public static DirectChatRoom create(User requester, User receiver) {
        return new DirectChatRoom(requester, receiver);
    }

    public boolean hasParticipant(Long userId) {
        return requester.getUserId().equals(userId) || receiver.getUserId().equals(userId);
    }

    public User getOpponent(Long userId) {
        if (requester.getUserId().equals(userId)) {
            return receiver;
        }
        return requester;
    }
}
