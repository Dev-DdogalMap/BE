package com.ddogalmap.domain.chat.entity;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "direct_chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long directChatMessageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direct_chat_room_id", nullable = false)
    private DirectChatRoom directChatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatMessageType messageType;

    @Lob
    @Column(nullable = false)
    private String content;

    private DirectChatMessage(
            DirectChatRoom directChatRoom,
            User sender,
            ChatMessageType messageType,
            String content
    ) {
        this.directChatRoom = directChatRoom;
        this.sender = sender;
        this.messageType = messageType;
        this.content = content;
    }

    public static DirectChatMessage create(
            DirectChatRoom directChatRoom,
            User sender,
            ChatMessageType messageType,
            String content
    ) {
        return new DirectChatMessage(directChatRoom, sender, messageType, content);
    }
}
