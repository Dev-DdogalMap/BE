package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.repository.RestaurantStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 식당 통계 계산 + restaurant_stats 테이블에 UPSERT.
 * - 대량 처리 대비해 청크 단위로 SQL 실행
 * - 한 청크는 한 트랜잭션에서 단일 INSERT ... ON CONFLICT 로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantStatsCalculator {

    /** 한 SQL에 넣을 식당 ID 개수. IN 절 너무 크면 plan 비용 증가. */
    private static final int CHUNK_SIZE = 1000;

    private final RestaurantStatsRepository statsRepository;

    /**
     * 주어진 식당 ID들의 통계를 재계산하여 UPSERT.
     * @return 처리된 식당 수
     */
    @Transactional
    public int recalculate(List<Long> restaurantIds) {
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (int from = 0; from < restaurantIds.size(); from += CHUNK_SIZE) {
            int to = Math.min(from + CHUNK_SIZE, restaurantIds.size());
            List<Long> chunk = restaurantIds.subList(from, to);
            int affected = statsRepository.upsertStatsForRestaurantIds(chunk);
            total += affected;
            log.info("[RestaurantStatsCalculator] chunk {} ~ {} 처리, affected={}",
                    from, to, affected);
        }
        return total;
    }

    /**
     * 전체 식당 통계 재계산 (최초 1회 또는 강제 갱신용).
     */
    @Transactional
    public int recalculateAll() {
        List<Long> allIds = statsRepository.findAllRestaurantIds();
        log.info("[RestaurantStatsCalculator] 전체 재계산 시작 - 대상 식당 {} 개", allIds.size());
        return recalculate(allIds);
    }
}
