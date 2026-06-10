package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.event.RestaurantStatsRefreshEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * {@link RestaurantStatsRefreshEvent} 처리 리스너.
 *
 * - AFTER_COMMIT: 발행한 트랜잭션이 정상 commit 된 후에만 실행 (rollback 시 무시)
 * - @Async: 호출 쓰레드(후기 작성 API 등)와 분리 → 응답 속도에 영향 없음
 * - 실패해도 원본 트랜잭션엔 영향 없음. 누락 시 스케줄러가 새벽 3시에 복구.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantStatsEventListener {

    private final RestaurantStatsCalculator calculator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRefresh(RestaurantStatsRefreshEvent event) {
        List<Long> ids = event.restaurantIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        try {
            int affected = calculator.recalculate(ids);
            log.info("[RestaurantStatsEventListener] 즉시 갱신 완료. 대상={}, affected={}",
                    ids.size(), affected);
        } catch (Exception e) {
            // 누락된 갱신은 새벽 3시 스케줄러가 복구하므로 여기서 throw 하지 않음
            log.error("[RestaurantStatsEventListener] 즉시 갱신 실패. 대상={}", ids, e);
        }
    }
}
