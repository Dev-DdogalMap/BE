package com.ddogalmap.domain.levels.repository;

import com.ddogalmap.domain.levels.entity.UserLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLevelHistoryRepository extends JpaRepository<UserLevelHistory, Long> {

    boolean existsByUserUserIdAndLevelPolicyLevelPolicyIdAndReferenceId(Long userId, Long levelPolicyId, Long referenceId);
}
