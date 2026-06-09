package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.repository.RestaurantStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 식당 통계 계산 + restaurant_stats 테이블에 UPSERT.
 * - 대량 처리 대비해 청크 단위로 SQL 실행
 * - 각 청크는 {@link RestaurantStatsChunkExecutor} 가 별도 트랜잭션(REQUIRES_NEW)에서 처리
 *   → 한 청크 실패해도 다른 청크 영향 없음, long transaction 회피
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantStatsCalculator {

    /** 한 SQL에 넣을 식당 ID 개수. IN 절 너무 크면 plan 비용 증가. */
    private static final int CHUNK_SIZE = 1000;

    private final RestaurantStatsRepository statsRepository;
    private final RestaurantStatsChunkExecutor chunkExecutor;

    /**
     * 주어진 식당 ID들의 통계를 재계산하여 UPSERT.
     * - 청크별로 별도 트랜잭션에서 처리 → 부분 실패 허용
     * @return 처리된 식당 수
     */
    public int recalculate(List<Long> restaurantIds) {
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (int from = 0; from < restaurantIds.size(); from += CHUNK_SIZE) {
            int to = Math.min(from + CHUNK_SIZE, restaurantIds.size());
            List<Long> chunk = restaurantIds.subList(from, to);
            try {
                int affected = chunkExecutor.upsertChunk(chunk);
                total += affected;
                log.info("[RestaurantStatsCalculator] chunk {} ~ {} 처리, affected={}",
                        from, to, affected);
            } catch (Exception e) {
                log.error("[RestaurantStatsCalculator] chunk {} ~ {} 실패 (다음 청크 계속 진행)",
                        from, to, e);
            }
        }
        return total;
    }

    /**
     * 전체 식당 통계 재계산 (최초 1회 또는 강제 갱신용).
     * 동기 호출. 청크별 별도 트랜잭션.
     */
    public int recalculateAll() {
        List<Long> allIds = statsRepository.findAllRestaurantIds();
        log.info("[RestaurantStatsCalculator] 전체 재계산 시작 - 대상 식당 {} 개", allIds.size());
        return recalculate(allIds);
    }

    /**
     * 전체 식당 통계 재계산을 백그라운드로 비동기 실행.
     * 호출 즉시 반환 → admin API 가 HTTP 요청 쓰레드를 점유하지 않음.
     * 결과는 로그로만 확인.
     */
    @Async
    public void recalculateAllAsync() {
        long start = System.currentTimeMillis();
        try {
            int affected = recalculateAll();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[RestaurantStatsCalculator] 비동기 전체 재계산 완료. 처리={} 식당, 소요={}ms",
                    affected, elapsed);
        } catch (Exception e) {
            log.error("[RestaurantStatsCalculator] 비동기 전체 재계산 실패", e);
        }
    }
}
