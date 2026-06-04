package com.ddogalmap.domain.chat.entity;

import com.ddogalmap.domain.chat.enumtype.ChatMessageType;
import com.ddogalmap.domain.chat.enumtype.Status;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Entity
@Table(name = "chat_messages")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatMessages extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_chat_room_id")
    private DirectChatRoom directChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRooms chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "writer", nullable = false)
    private User writer;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatMessageType messageType = ChatMessageType.TEXT;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.SENT;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    private ChatMessages(
            DirectChatRoom directChatRoom,
            User writer,
            ChatMessageType messageType,
            Status status,
            String message
    ) {
        this.directChatRoom = directChatRoom;
        this.writer = writer;
        this.messageType = messageType;
        this.status = status;
        this.message = message;
    }

    public static ChatMessages create(
            DirectChatRoom directChatRoom,
            User writer,
            ChatMessageType messageType,
            Status status,
            String message
    ) {
        return new ChatMessages(directChatRoom, writer, messageType, status, message);
    }
}
