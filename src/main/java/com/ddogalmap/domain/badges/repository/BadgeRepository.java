package com.ddogalmap.domain.badges.repository;


import com.ddogalmap.domain.badges.entity.Badge;
import com.ddogalmap.domain.badges.enumtype.BadgeConditionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByConditionType(BadgeConditionType conditionType);
}
