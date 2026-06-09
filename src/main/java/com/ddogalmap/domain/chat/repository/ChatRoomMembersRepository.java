package com.ddogalmap.domain.chat.repository;

import com.ddogalmap.domain.chat.dto.groupChat.response.MemberDetailInfo;
import com.ddogalmap.domain.chat.dto.groupChat.response.MemberInfo;
import com.ddogalmap.domain.chat.entity.ChatRoomMembers;
import com.ddogalmap.domain.chat.entity.ChatRooms;
import com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Query("""
            select new com.ddogalmap.domain.chat.dto.groupChat.response.MemberDetailInfo(
                    u.userId,
                    u.profileImageUrl,
                    u.nickname,
                    COALESCE(lv.level, 1),
                    crm.role
            )
            from ChatRoomMembers crm
            join crm.user u
            left join UserLevel ul on ul.user = u
            left join ul.level lv
            where crm.chatRoom = :chatRooms
            """)
    List<MemberDetailInfo> findAllMembersByChatRoom(@Param("chatRooms") ChatRooms chatRooms);

    Optional<ChatRoomMembers> findByChatRoom_idAndUser_userId(Long roomId, Long userId);

    //OWNER 권한 멤버 조회 - 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select crm
    from ChatRoomMembers crm
    where crm.chatRoom.id = :roomId
      and crm.role = com.ddogalmap.domain.chat.enumtype.ChatRoomMemberRole.OWNER
""")
    List<ChatRoomMembers> findOwnersForUpdate(Long roomId);

    Optional<ChatRoomMembers> findByChatRoom_idAndUser_UserId(Long roomId, Long userId);

    void deleteAllByChatRoom_idAndUser_UserIdIn(Long roomId, List<Long> userIds);

    @Query("""
        select crm
        from ChatRoomMembers crm
        where crm.chatRoom.id = :roomId
        and crm.user.userId in :userIds
""")
    List<ChatRoomMembers> findGrantedMember(Long roomId, List<Long> userIds);
}