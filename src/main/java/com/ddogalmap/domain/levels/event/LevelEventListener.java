package com.ddogalmap.domain.levels.event;

import com.ddogalmap.domain.levels.dto.LevelExpEvent;
import com.ddogalmap.domain.levels.service.LevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
@Slf4j
public class LevelEventListener {

    private final LevelService levelService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(LevelExpEvent event) {
        log.info("[LevelEvent] 경험치 적립 이벤트 수신 userId={}, activityType={}",
                event.userId(), event.activityType());

        try {
            levelService.addExp(event.userId(), event.activityType(), event.referenceId());
        } catch (Exception e) {
            log.error("[LevelEvent] 경험치 적립 실패 userId={}, activityType={}",
                    event.userId(), event.activityType(), e);
        }
    }
}
