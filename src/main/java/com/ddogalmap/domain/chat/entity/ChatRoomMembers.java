package com.ddogalmap.domain.chat.entity;

import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;
import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "chat_room_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_member",
                        columnNames = {"chat_room_id", "user_id"}
                )
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoomMembers extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRooms chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ChatRoomMemberRole role;

    public void grantOwner() {
        this.role = ChatRoomMemberRole.OWNER;
    }
}
