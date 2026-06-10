package com.ddogalmap.domain.chat.entity;

import com.ddogalmap.domain.chat.enumtype.Status;
import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "chat_messages",
        //메세지 목록 조회를 위한 복합 인덱스
        indexes = @Index(
                name = "idx_chat_messages_room_id",
                columnList = "chat_room_id, chat_message_id DESC"
        )
)
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
    private Status status = Status.SENT;

    @Column(nullable = false, length = 255)
    private String message;

    private ChatMessages(
            DirectChatRoom directChatRoom,
            User writer,
            Status status,
            String message
    ) {
        this.directChatRoom = directChatRoom;
        this.writer = writer;
        this.status = status;
        this.message = message;
    }

    public static ChatMessages create(
            DirectChatRoom directChatRoom,
            User writer,
            Status status,
            String message
    ) {
        return new ChatMessages(directChatRoom, writer, status, message);
    }
}
