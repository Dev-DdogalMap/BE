package com.ddogalmap.domain.restaurants.event;

import java.util.List;

/**
 * restaurant_stats 즉시 갱신 트리거 이벤트.
 *
 * 후기 작성 / 방문 인증 등 통계 산식 입력 데이터가 바뀌는 시점에 발행.
 * 리스너({@link com.ddogalmap.domain.restaurants.service.RestaurantStatsEventListener})
 * 가 트랜잭션 commit 후 비동기로 받아서 해당 식당들의 stats 를 재계산한다.
 *
 * - 트랜잭션 안에서 발행 → AFTER_COMMIT 단계에서만 처리됨 (rollback 시 무시)
 * - 비동기(@Async) → 후기 작성 API 응답 속도에 영향 주지 않음
 */
public record RestaurantStatsRefreshEvent(List<Long> restaurantIds) {
}
