package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.entity.ChatRoomMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomMembersRepository extends JpaRepository<ChatRoomMembers, Long> {

    boolean existsByChatRoom_idAndUser_UserId(Long chatRoomId, Long userId);
}