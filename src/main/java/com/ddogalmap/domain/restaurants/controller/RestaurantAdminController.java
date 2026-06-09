package com.ddogalmap.domain.restaurants.controller;

import com.ddogalmap.domain.restaurants.dto.ImportResult;
import com.ddogalmap.domain.restaurants.service.RestaurantImportService;
import com.ddogalmap.domain.restaurants.service.RestaurantStatsCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Restaurant Admin", description = "음식점 데이터 적재 (관리용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/restaurants")
public class RestaurantAdminController {

    private final RestaurantImportService importService;
    private final RestaurantStatsCalculator statsCalculator;

    @Operation(
            summary = "서울시 일반음식점 인허가 데이터 일괄 적재",
            description = """
                    서울 열린데이터광장 OpenAPI(OA-16094)를 호출하여 영업/정상(01) + 휴업(02) 음식점만 DB에 적재합니다.

                    - 페이지당 1,000건씩 호출 (전체 약 53만 건 → 영업·휴업만 약 12~15만 건)
                    - food_type 자동 생성
                    - 좌표(EPSG:5174) → WGS84(4326) 변환 (PostGIS ST_Transform)
                    - 중복(management_no 기준)은 ON CONFLICT DO NOTHING 으로 자동 스킵
                    - 예상 시간 20~30분
                    """
    )
    @PostMapping("/import")
    public ImportResult importRestaurants() {
        return importService.importAll();
    }

    @Operation(
            summary = "행정안전부 식품_일반음식점(전국) 데이터 일괄 적재",
            description = """
                    행정안전부 식품_일반음식점 조회서비스 OpenAPI를 호출하여 전국 음식점 데이터를 적재합니다.

                    - Endpoint: https://apis.data.go.kr/1741000/general_restaurants/info
                    - 페이지당 100건씩 호출 (API max numOfRows = 100)
                    - 영업/정상(01)만 적재 (휴업/폐업 제외)
                    - 좌표(x, y) 둘 다 있는 행만 적재
                    - food_type 자동 생성 (BZSTAT_SE_NM 기반)
                    - 좌표(EPSG:5174) → WGS84(4326) 변환 (PostGIS ST_Transform)
                    - 기존 서울 데이터와 통합 (management_no 기준 ON CONFLICT DO NOTHING)
                    - 일일 트래픽 10000 한도로 중단된 경우 startPage 파라미터로 이어받기 가능
                    """
    )
    @PostMapping("/import-all")
    public ImportResult importAllRestaurants(
            @Parameter(description = "시작 페이지 (1부터, 일일 한도 도달 후 이어받기 시 마지막 페이지+1 입력)")
            @RequestParam(defaultValue = "1") int startPage
    ) {
        return importService.importAllNational(startPage);
    }

    @Operation(
            summary = "찐맛집지수 통계 전체 재계산 (restaurant_stats)",
            description = """
                    모든 식당에 대해 찐맛집지수/주민추천비율/재방문율/방문인증수/즐겨찾기수/평균별점/리뷰개수를
                    다시 계산해서 restaurant_stats 테이블에 UPSERT.

                    - 최초 1회 (배치 초기화) 또는 산식 변경 후 강제 갱신용
                    - 일배치 스케줄러는 매일 새벽 3시에 incremental 처리됨
                    - 청크 단위(1000개)로 처리
                    """
    )
    @PostMapping("/stats/recalculate-all")
    public int recalculateAllStats() {
        return statsCalculator.recalculateAll();
    }
}
