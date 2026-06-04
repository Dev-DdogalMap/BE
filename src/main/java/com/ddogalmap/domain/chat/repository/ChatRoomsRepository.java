package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.dto.groupChat.response.ChatRoomListThumbnailResponse;
import com.ddogalmap.domain.chat.entity.ChatRooms;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
