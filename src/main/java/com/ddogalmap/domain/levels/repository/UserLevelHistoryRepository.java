package com.ddogalmap.domain.levels.repository;

import com.ddogalmap.domain.levels.dto.projection.LevelHistoryProjection;
import com.ddogalmap.domain.levels.entity.UserLevelHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserLevelHistoryRepository extends JpaRepository<UserLevelHistory, Long> {

    boolean existsByUserUserIdAndLevelPolicyLevelPolicyIdAndReferenceId(Long userId, Long levelPolicyId, Long referenceId);

    @Query("""
        select
            h.userLevelHistoryId as historyId,
            lp.activityType as activityType,
            h.expAmount as expAmount,
            la.level as level,
            la.name as levelName,
            h.createdAt as createdAt
        from UserLevelHistory h
        join h.levelPolicy lp
        join h.levelAfter la
        where h.user.userId = :userId
        order by h.createdAt desc
    """)
    List<LevelHistoryProjection> findRecentLevelHistories(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
