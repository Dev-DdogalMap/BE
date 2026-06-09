package com.ddogalmap.domain.users.repository;

import com.ddogalmap.domain.users.dto.projection.UserStatsProjection;
import com.ddogalmap.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);

    /**
     * 사용자의 인증된 동네(region) 만 가벼운 쿼리로 조회.
     * 인증 안 했거나 region 비어있으면 빈 Optional.
     */
    @Query("SELECT u.region FROM User u WHERE u.userId = :userId")
    Optional<String> findRegionByUserId(@Param("userId") Long userId);

    @Query("""
        select u
        from User u
        left join fetch u.representativeBadge
        where u.userId = :userId
    """)
    Optional<User> findByIdWithRepresentativeBadge(@Param("userId") Long userId);

    @Query(value = """
        SELECT
            (SELECT COUNT(*)
             FROM visit_verifications v
             WHERE v.user_id = :userId) AS visitCount,

            (SELECT COUNT(*)
             FROM reviews r
             WHERE r.user_id = :userId) AS reviewCount,

            (SELECT COUNT(*)
             FROM bookmarks b
             WHERE b.user_id = :userId) AS bookmarkCount,

            (SELECT COUNT(*)
             FROM chat_room_members c
             WHERE c.user_id = :userId) AS chatRoomCount
        """, nativeQuery = true)
    UserStatsProjection getUserStats(Long userId);
}