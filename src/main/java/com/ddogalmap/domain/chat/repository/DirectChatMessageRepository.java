package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.entity.DirectChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DirectChatMessageRepository extends JpaRepository<DirectChatMessage, Long> {

    @EntityGraph(attributePaths = {"sender", "directChatRoom"})
    @Query("""
            select m
            from DirectChatMessage m
            where m.directChatRoom.directChatRoomId = :roomId
            order by m.createdAt desc, m.directChatMessageId desc
            """)
    List<DirectChatMessage> findRecentMessages(@Param("roomId") Long roomId, Pageable pageable);

    @Query("""
            select m
            from DirectChatMessage m
            where m.directChatRoom.directChatRoomId = :roomId
            order by m.createdAt desc, m.directChatMessageId desc
            """)
    List<DirectChatMessage> findAllByRoomOrderByRecent(@Param("roomId") Long roomId);

    @EntityGraph(attributePaths = {"sender", "directChatRoom"})
    Optional<DirectChatMessage> findTopByDirectChatRoom_DirectChatRoomIdOrderByCreatedAtDescDirectChatMessageIdDesc(Long roomId);
}
