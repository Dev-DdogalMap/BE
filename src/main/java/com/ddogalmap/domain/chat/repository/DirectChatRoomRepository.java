package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.dto.response.MyChatRoomResponse;
import com.ddogalmap.domain.chat.entity.DirectChatRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DirectChatRoomRepository extends JpaRepository<DirectChatRoom, Long> {

    @EntityGraph(attributePaths = {"requester", "receiver"})
    @Query("""
            select d
            from DirectChatRoom d
            where d.deletedAt is null
              and (
                   (d.requester.userId = :userA and d.receiver.userId = :userB)
                or (d.requester.userId = :userB and d.receiver.userId = :userA)
              )
            """)
    Optional<DirectChatRoom> findBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);

    @EntityGraph(attributePaths = {"requester", "receiver"})
    @Query("""
            select d
            from DirectChatRoom d
            where d.deletedAt is null
              and (
                   (d.requester.userId = :userId and d.requesterLeftAt is null)
                or (d.receiver.userId = :userId and d.receiverLeftAt is null)
              )
            order by d.updatedAt desc, d.directChatRoomId desc
            """)
    List<DirectChatRoom> findAllByParticipant(@Param("userId") Long userId);

    //내 대화 목록 조회
    @Query("""
    SELECT new com.ddogalmap.domain.chat.dto.response.MyChatRoomResponse(
        dcr.directChatRoomId,
        CASE WHEN dcr.requester.userId = :currentUserId 
             THEN dcr.receiver.userId 
             ELSE dcr.requester.userId END,
	        CASE
	             WHEN dcr.requester.userId = :currentUserId AND dcr.receiverLeftAt IS NOT NULL
	             THEN '대화 상대 없음'
	             WHEN dcr.requester.userId = :currentUserId AND dcr.receiver.status = 'DELETED'
	             THEN '대화 상대 없음'
	             WHEN dcr.receiver.userId = :currentUserId AND dcr.requesterLeftAt IS NOT NULL
	             THEN '대화 상대 없음'
	             WHEN dcr.receiver.userId = :currentUserId AND dcr.requester.status = 'DELETED'
	             THEN '대화 상대 없음'
	             WHEN dcr.requester.userId = :currentUserId
	             THEN dcr.receiver.nickname
	             ELSE dcr.requester.nickname
	        END,
	        CASE
	             WHEN dcr.requester.userId = :currentUserId AND dcr.receiverLeftAt IS NOT NULL
	             THEN NULL
	             WHEN dcr.requester.userId = :currentUserId AND dcr.receiver.status = 'DELETED'
	             THEN NULL
	             WHEN dcr.receiver.userId = :currentUserId AND dcr.requesterLeftAt IS NOT NULL
	             THEN NULL
	             WHEN dcr.receiver.userId = :currentUserId AND dcr.requester.status = 'DELETED'
	             THEN NULL
	             WHEN dcr.requester.userId = :currentUserId
	             THEN dcr.receiver.profileImageUrl
             ELSE dcr.requester.profileImageUrl
        END,
        (SELECT dm.message FROM ChatMessages dm 
             WHERE dm.directChatRoom = dcr 
             ORDER BY dm.createdAt DESC, dm.chatMessageId DESC LIMIT 1),
        (SELECT MAX(dm.createdAt) FROM ChatMessages dm 
             WHERE dm.directChatRoom = dcr),
        0,
        dcr.createdAt,
        "DIRECT"
    )
    FROM DirectChatRoom dcr
    WHERE dcr.deletedAt IS NULL
      AND (
           (dcr.requester.userId = :currentUserId AND dcr.requesterLeftAt IS NULL)
        OR (dcr.receiver.userId = :currentUserId AND dcr.receiverLeftAt IS NULL)
      )
""")
    List<MyChatRoomResponse> findAllByParticipantWithLatestMessage(
            @Param("currentUserId") Long currentUserId
    );

    @EntityGraph(attributePaths = {"requester", "receiver"})
    @Query("""
            select d
            from DirectChatRoom d
            where d.deletedAt is null
              and (
                   (d.requester.userId = :userId and d.requesterLeftAt is null)
                or (d.receiver.userId = :userId and d.receiverLeftAt is null)
              )
            """)
    List<DirectChatRoom> findActiveRoomsByParticipant(@Param("userId") Long userId);

    int countByReceiverUserId(Long receiverId);
}
