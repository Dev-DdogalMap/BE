package com.ddogalmap.domain.restaurants.dto;

/**
 * 적재 결과 응답 DTO.
 *
 * - totalCount       : 서울 OpenAPI list_total_count (서울 전체 건수, 폐업 포함)
 * - totalFetched     : 페이지 호출로 실제 받은 row 수
 * - totalInserted    : DB에 실제 INSERT 된 건수 (ON CONFLICT 스킵 제외)
 * - skippedByState   : 영업/정상(01) + 휴업(02) 외 상태로 필터된 건수 (대부분 폐업)
 * - foodTypesCreated : 적재 중 새로 생성된 food_type 개수
 * - elapsedMs        : 총 소요 시간 (밀리초)
 */
public record ImportResult(
        int totalCount,
        int totalFetched,
        int totalInserted,
        int skippedByState,
        int foodTypesCreated,
        long elapsedMs
) {
}
