package com.ddogalmap.domain.badges.repository;

import com.ddogalmap.domain.badges.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    @Query("""
        select ub.badge.badgeId
        from UserBadge ub
        where ub.user.userId = :userId
    """)
    List<Long> findBadgeIdsByUserId(@Param("userId") Long userId);
}
