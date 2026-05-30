package com.ddogalmap.domain.chat.repository;

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
            where (d.requester.userId = :userA and d.receiver.userId = :userB)
               or (d.requester.userId = :userB and d.receiver.userId = :userA)
            """)
    Optional<DirectChatRoom> findBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);

    @EntityGraph(attributePaths = {"requester", "receiver"})
    @Query("""
            select d
            from DirectChatRoom d
            where d.requester.userId = :userId
               or d.receiver.userId = :userId
            order by d.updatedAt desc, d.directChatRoomId desc
            """)
    List<DirectChatRoom> findAllByParticipant(@Param("userId") Long userId);
}
