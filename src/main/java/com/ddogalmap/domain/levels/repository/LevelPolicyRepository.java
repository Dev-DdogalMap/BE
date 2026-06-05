package com.ddogalmap.domain.levels.repository;

import com.ddogalmap.domain.levels.entity.LevelPolicy;
import com.ddogalmap.domain.levels.enumtype.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelPolicyRepository extends JpaRepository<LevelPolicy, Long> {

    Optional<LevelPolicy> findByActivityType(ActivityType activityType);
}
