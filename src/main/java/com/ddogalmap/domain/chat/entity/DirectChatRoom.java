package com.ddogalmap.domain.chat.entity;

import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.Column;
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

import java.time.LocalDateTime;

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

    @Column(name = "requester_left_at")
    private LocalDateTime requesterLeftAt;

    @Column(name = "receiver_left_at")
    private LocalDateTime receiverLeftAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    public boolean isRequester(Long userId) {
        return requester.getUserId().equals(userId);
    }

    public boolean isReceiver(Long userId) {
        return receiver.getUserId().equals(userId);
    }

    public boolean hasLeft(Long userId) {
        if (isRequester(userId)) {
            return requesterLeftAt != null;
        }
        if (isReceiver(userId)) {
            return receiverLeftAt != null;
        }
        return false;
    }

    public boolean hasOpponentLeft(Long userId) {
        if (isRequester(userId)) {
            return receiverLeftAt != null;
        }
        if (isReceiver(userId)) {
            return requesterLeftAt != null;
        }
        return false;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isVisibleTo(Long userId) {
        return !isDeleted() && hasParticipant(userId) && !hasLeft(userId);
    }

    public void leave(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        if (isRequester(userId)) {
            requesterLeftAt = now;
        } else if (isReceiver(userId)) {
            receiverLeftAt = now;
        }

        if (requesterLeftAt != null && receiverLeftAt != null) {
            deletedAt = now;
        }
    }

    public void restore(Long userId) {
        if (isRequester(userId)) {
            requesterLeftAt = null;
        } else if (isReceiver(userId)) {
            receiverLeftAt = null;
        }
        deletedAt = null;
    }
}
