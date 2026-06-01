package com.ddogalmap.domain.restaurants.dto;

import java.util.List;

/**
 * 서울 열린데이터광장 한 페이지(1,000건) 응답 결과.
 */
public record SeoulOpenApiPageResponse(
        int totalCount,
        List<SeoulOpenApiRow> rows
) {
}
