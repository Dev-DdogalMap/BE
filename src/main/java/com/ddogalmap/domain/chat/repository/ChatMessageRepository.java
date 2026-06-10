package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.dto.groupChat.response.ChatMessageResponse;
import com.ddogalmap.domain.chat.entity.ChatMessages;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessages, Long> {

    @EntityGraph(attributePaths = {"writer", "directChatRoom"})
    @Query("""
            select m
            from ChatMessages m
            where m.directChatRoom.directChatRoomId = :roomId
            order by m.createdAt desc, m.chatMessageId desc
            """)
    List<ChatMessages> findRecentMessages(@Param("roomId") Long roomId, Pageable pageable);

    @Query("""
            select m
            from ChatMessages m
            where m.directChatRoom.directChatRoomId = :roomId
            order by m.createdAt desc, m.chatMessageId desc
            """)
    List<ChatMessages> findAllByRoomOrderByRecent(@Param("roomId") Long roomId);

    @EntityGraph(attributePaths = {"writer", "directChatRoom"})
    Optional<ChatMessages> findTopByDirectChatRoom_DirectChatRoomIdOrderByCreatedAtDescChatMessageIdDesc(Long roomId);

    @EntityGraph(attributePaths = {"writer", "chatRoom"})
    @Query("""
            select m
            from ChatMessages m
            where m.chatRoom.id = :roomId
            order by m.createdAt desc, m.chatMessageId desc
            """)
    List<ChatMessages> findGroupRecentMessages(@Param("roomId") Long roomId, Pageable pageable);

    @Query("""
            select new com.ddogalmap.domain.chat.dto.groupChat.response.ChatMessageResponse(
                        m.chatMessageId,
                        m.chatRoom.id,
                        u.userId,
                        u.nickname,
                        u.profileImageUrl,
                        COALESCE(lv.level, 1),
                        m.status,
                        m.message,
                        m.createdAt)
            from ChatMessages m
            join m.writer u
            left join UserLevel ul on ul.user = u
            left join ul.level lv
            where m.chatRoom.id = :roomId
            order by m.createdAt desc, m.chatMessageId desc
            """)
    List<ChatMessageResponse> findGroupRecentMessageV2(@Param("roomId") Long roomId, Pageable pageable);

    //커서 기반 메세지 목록 조회
    @Query("""
    select new com.ddogalmap.domain.chat.dto.groupChat.response.ChatMessageResponse(
                m.chatMessageId,
                m.chatRoom.id,
                u.userId,
                u.nickname,
                u.profileImageUrl,
                COALESCE(lv.level, 1),
                m.status,
                m.message,
                m.createdAt)
    from ChatMessages m
    join m.writer u
    left join UserLevel ul on ul.user = u
    left join ul.level lv
    where m.chatRoom.id = :roomId
      and (:cursorId is null or m.chatMessageId < :cursorId)
    order by m.chatMessageId desc
    """)
    List<ChatMessageResponse> findMessagesWithCursor(
            @Param("roomId") Long roomId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
