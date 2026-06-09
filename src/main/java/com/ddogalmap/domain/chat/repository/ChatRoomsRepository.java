package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.dto.groupChat.response.ChatRoomListThumbnailResponse;
import com.ddogalmap.domain.chat.dto.response.DirectChatRoomResponse;
import com.ddogalmap.domain.chat.dto.response.MyChatRoomResponse;
import com.ddogalmap.domain.chat.entity.ChatRooms;
import com.ddogalmap.domain.users.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRoomsRepository extends JpaRepository<ChatRooms, Long> {

    @Modifying
    @Query("""
    update ChatRooms c
    set c.participantCount = c.participantCount + 1
    where c.id = :roomId
""")
    void increaseParticipantCount(Long roomId);  //동시성 문제를 막기 위한 원자적 update

    @Query("""
        SELECT new com.ddogalmap.domain.chat.dto.groupChat.response.ChatRoomListThumbnailResponse(
            cr.id,
            cr.imageUrl,
            cr.roomName,
            cr.participantCount,
            cr.maxParticipantCount,
            cr.createdAt,
            (SELECT MAX(cm.createdAt) FROM ChatMessages cm WHERE cm.chatRoom = cr)
        )
        FROM ChatRooms cr
        ORDER BY cr.createdAt DESC
""")
    Slice<ChatRoomListThumbnailResponse> findChatRoomListThumbnail(Pageable pageable);

    @Query("""
                    SELECT new com.ddogalmap.domain.chat.dto.response.MyChatRoomResponse(
                        cr.id,
                        null,
                        cr.roomName,
                        cr.imageUrl,
                        (SELECT cm.message FROM ChatMessages cm WHERE cm.chatRoom = cr ORDER BY cm.createdAt DESC LIMIT 1),
                        (SELECT MAX(cm.createdAt) FROM ChatMessages cm WHERE cm.chatRoom = cr),
                        0,
                        cr.createdAt,
                        "GROUP"
                    )
                    FROM ChatRooms cr
                    JOIN ChatRoomMembers crm on crm.chatRoom = cr
                    WHERE crm.user = :user
                    ORDER BY cr.createdAt DESC
            """)
    List<MyChatRoomResponse> findMyChatRooms(User user);

    @Modifying
    @Query("UPDATE ChatRooms c SET c.participantCount = c.participantCount - 1 WHERE c.id = :roomId")
    void decreaseParticipantCount(@Param("roomId") Long roomId);
}
