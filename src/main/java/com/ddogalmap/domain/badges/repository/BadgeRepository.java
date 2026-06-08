package com.ddogalmap.domain.badges.repository;


import com.ddogalmap.domain.badges.dto.projection.BadgeDetailProjection;
import com.ddogalmap.domain.badges.entity.Badge;
import com.ddogalmap.domain.badges.enumtype.BadgeConditionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByConditionType(BadgeConditionType conditionType);

    @Query("""
        select
            b.badgeId        as badgeId,
            b.name           as name,
            b.iconImage      as iconImage,
            b.conditionType  as conditionType,
            b.conditionValue as conditionValue,
            case when ub.userBadgeId is not null then true else false end as acquired,
            ub.createdAt     as acquiredAt
        from Badge b
        left join UserBadge ub
            on ub.badge = b and ub.user.userId = :userId
        order by
            case when ub.userBadgeId is not null then 0 else 1 end,
            ub.createdAt desc,
            b.badgeId asc
    """)
    List<BadgeDetailProjection> findBadgeDetailsByUserId(@Param("userId") Long userId);
}
