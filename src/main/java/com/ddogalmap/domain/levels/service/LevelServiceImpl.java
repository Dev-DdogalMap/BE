package com.ddogalmap.domain.levels.service;

import com.ddogalmap.domain.levels.entity.Level;
import com.ddogalmap.domain.levels.entity.LevelPolicy;
import com.ddogalmap.domain.levels.entity.UserLevel;
import com.ddogalmap.domain.levels.entity.UserLevelHistory;
import com.ddogalmap.domain.levels.enumtype.ActivityType;
import com.ddogalmap.domain.levels.repository.LevelPolicyRepository;
import com.ddogalmap.domain.levels.repository.LevelRepository;
import com.ddogalmap.domain.levels.repository.UserLevelHistoryRepository;
import com.ddogalmap.domain.levels.repository.UserLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class LevelServiceImpl implements LevelService {

    private final LevelPolicyRepository levelPolicyRepository;
    private final UserLevelRepository userLevelRepository;
    private final LevelRepository levelRepository;
    private final UserLevelHistoryRepository userLevelHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addExp(Long userId, ActivityType activityType, Long referenceId) {

        log.info("[LevelService] 경험치 적립 시작 userId={}, activityType={}", userId, activityType);

        LevelPolicy levelPolicy = levelPolicyRepository.findByActivityType(activityType)
                .orElseThrow(() -> new IllegalArgumentException("레벨 정책을 찾을 수 없습니다. activityType=" + activityType));

        UserLevel userLevel = userLevelRepository.findForUpdate(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 레벨이 없습니다. userId=" + userId));

        if (referenceId != null &&
                userLevelHistoryRepository.existsByUserUserIdAndLevelPolicyLevelPolicyIdAndReferenceId(
                        userId,
                        levelPolicy.getLevelPolicyId(),
                        referenceId
                )) {
            log.info("[LevelService] 이미 지급된 경험치입니다. userId={}, activityType={}, referenceId={}",
                    userId, activityType, referenceId);
            return;
        }

        int beforeExp = userLevel.getExp();
        Level beforeLevel = userLevel.getLevel();

        int afterExp = beforeExp + levelPolicy.getExp();
        Level afterLevel = levelRepository.findTopByRequiredExpLessThanEqualOrderByRequiredExpDesc(afterExp)
                .orElseThrow(() -> new IllegalArgumentException("해당 경험치에 맞는 레벨을 찾을 수 없습니다. exp=" + afterExp));


        userLevel.updateExpAndLevel(afterExp, afterLevel);

        userLevelHistoryRepository.save(
                UserLevelHistory.create(
                        userLevel.getUser(),
                        levelPolicy,
                        levelPolicy.getExp(),
                        afterExp,
                        afterLevel,
                        referenceId
                )
        );

        log.info("[LevelService] 경험치 적립 완료 userId={}, activityType={}", userId, activityType);

        if(!beforeLevel.getLevelId().equals(afterLevel.getLevelId())) {
            log.info("[LevelService] 레벨업 발생 userId={}, {} -> {}", userId, beforeLevel.getLevel(), afterLevel.getLevel());
        }
    }
}
