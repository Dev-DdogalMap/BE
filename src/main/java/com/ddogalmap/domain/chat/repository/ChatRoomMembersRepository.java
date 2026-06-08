package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.dto.groupChat.response.MemberInfo;
import com.ddogalmap.domain.chat.entity.ChatRoomMembers;
import com.ddogalmap.domain.chat.entity.ChatRooms;
import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomMembersRepository extends JpaRepository<ChatRoomMembers, Long> {

    boolean existsByChatRoom_idAndUser_UserId(Long chatRoomId, Long userId);

    @Query("""
        select new com.ddogalmap.domain.chat.dto.groupChat.response.MemberInfo(u.userId, u.profileImageUrl)
        from ChatRoomMembers crm
        join crm.user u
        where crm.chatRoom = :chatRooms
""")
    List<MemberInfo> findAllByChatRoom(@Param("chatRooms") ChatRooms chatRooms);

    //owner 검증
    Boolean existsByChatRoom_idAndUser_UserIdAndRole(Long chatRoomId, Long userId, ChatRoomMemberRole role);
}