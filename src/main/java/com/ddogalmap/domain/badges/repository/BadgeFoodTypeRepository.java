package com.ddogalmap.domain.badges.repository;

import com.ddogalmap.domain.badges.entity.Badge;
import com.ddogalmap.domain.badges.entity.BadgeFoodType;
import com.ddogalmap.domain.badges.enumtype.BadgeConditionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BadgeFoodTypeRepository extends JpaRepository<BadgeFoodType, Long> {

    @Query("""
        select distinct bft.badge
        from BadgeFoodType bft
        where bft.foodType.foodTypeId = :foodTypeId
          and bft.badge.conditionType = :conditionType
    """)
    List<Badge> findBadgesByFoodTypeIdAndConditionType(
            @Param("foodTypeId") Long foodTypeId,
            @Param("conditionType") BadgeConditionType conditionType
    );

    @Query("""
        select bft
        from BadgeFoodType bft
        join fetch bft.badge
        join fetch bft.foodType
    """)
    List<BadgeFoodType> findAllWithBadgeAndFoodType();
}
