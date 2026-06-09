package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.repository.RestaurantStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 식당 통계 (restaurant_stats) 일배치 스케줄러.
 *
 * - 매일 새벽 3시에 실행
 * - 마지막 배치 이후 활동(reviews / visit_verifications)이 있었던
 *   식당만 재계산 (incremental)
 *   · bookmarks 는 산식에 포함되지 않으므로 트리거에서 제외
 * - 처음 실행 시 (stats 비어있음)에는 전체 식당 일괄 계산
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantStatsScheduler {

    private final RestaurantStatsRepository statsRepository;
    private final RestaurantStatsCalculator calculator;

    /**
     * 매일 새벽 03:00 실행.
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void runDailyBatch() {
        long start = System.currentTimeMillis();
        try {
            Optional<LocalDateTime> lastRun = statsRepository.findLastBatchTime();

            int affected;
            if (lastRun.isEmpty()) {
                log.info("[RestaurantStatsScheduler] stats 비어있음 → 전체 재계산");
                affected = calculator.recalculateAll();
            } else {
                LocalDateTime since = lastRun.get();
                List<Long> changedIds = statsRepository.findChangedRestaurantIdsSince(since);
                log.info("[RestaurantStatsScheduler] 마지막 배치={} 이후 활동 있던 식당 {} 개",
                        since, changedIds.size());
                affected = calculator.recalculate(changedIds);
            }

            long elapsed = System.currentTimeMillis() - start;
            log.info("[RestaurantStatsScheduler] 완료. 처리={} 식당, 소요={}ms",
                    affected, elapsed);
        } catch (Exception e) {
            log.error("[RestaurantStatsScheduler] 배치 실패", e);
        }
    }
}
