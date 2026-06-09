package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.repository.RestaurantStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * restaurant_stats UPSERT 를 청크 단위로 별도 트랜잭션에서 실행.
 *
 * - Propagation.REQUIRES_NEW: 청크 1개를 항상 새 트랜잭션에서 처리
 *   · 한 청크가 실패해도 다른 청크에 영향 없음
 *   · 30만 식당 일괄 처리 시 long transaction (lock/메모리 누적) 회피
 * - Self-invocation 회피를 위해 {@link RestaurantStatsCalculator} 와 별도 빈으로 분리
 *   (같은 클래스 안에서 @Transactional 메서드를 호출하면 프록시를 거치지 않음)
 */
@Component
@RequiredArgsConstructor
public class RestaurantStatsChunkExecutor {

    private final RestaurantStatsRepository statsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int upsertChunk(List<Long> chunk) {
        return statsRepository.upsertStatsForRestaurantIds(chunk);
    }
}
