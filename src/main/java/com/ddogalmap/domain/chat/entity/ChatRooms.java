package com.ddogalmap.domain.chat.entity;

import com.ddogalmap.domain.chat.dto.groupChat.request.UpdateChatRoomRequest;
import com.ddogalmap.domain.foodtypes.entity.FoodType;
import com.ddogalmap.domain.users.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "chat_rooms")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRooms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @Column(name = "room_name", length = 100)
    private String roomName;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "participant_count")
    private Integer participantCount;

    @Column(name = "max_participant_count")
    private Integer maxParticipantCount;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_type_id")
    private FoodType foodType;

    public void updateChatRoom(UpdateChatRoomRequest request, String imageKey, FoodType foodType) {
        this.roomName = request.roomName();
        this.region = request.region();
        this.maxParticipantCount = request.maxParticipantCount();
        this.foodType = foodType;
        this.imageUrl = imageKey;
    }
}
