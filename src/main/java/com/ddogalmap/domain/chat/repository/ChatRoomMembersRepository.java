package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.entity.ChatRoomMembers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMembersRepository extends JpaRepository<ChatRoomMembers, Long> {
}
