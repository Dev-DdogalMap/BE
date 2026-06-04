package com.ddogalmap.domain.restaurants.controller;

import com.ddogalmap.domain.restaurants.dto.response.RestaurantSearchResponse;
import com.ddogalmap.domain.restaurants.service.RestaurantSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Restaurant Search", description = "맛집 검색")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class RestaurantSearchController {

    private final RestaurantSearchService restaurantSearchService;

    @Operation(
            summary = "맛집 목록 검색 (필터링/정렬)",
            description = """
                    필터:
                    - keyword: 음식점 이름에 포함되는 텍스트
                    - region: 지번 주소에 포함되는 텍스트
                    - foodTypeId: 음식 종류 ID (food_types 테이블 참조)
                    - lat, lng: 사용자 좌표 (거리 계산용, 선택)
                      값이 없으면 distance 정렬 시 jjinScore 정렬로 자동 폴백

                    정렬 (sort):
                    - distance (기본): 거리 가까운 순 → 같으면 맛집 지수 높은 순
                    - jjinScore: 맛집 지수 높은 순 → 같으면 거리 가까운 순
                    - score: 별점 평균 높은 순 → 같으면 거리 가까운 순

                    페이지: page=1 부터, size 기본 20
                    """
    )
    @GetMapping("/search")
    public RestaurantSearchResponse search(
            @Parameter(description = "검색어 (음식점 이름)") @RequestParam(required = false) String keyword,
            @Parameter(description = "지역 (지번 주소 매칭)") @RequestParam(required = false) String region,
            @Parameter(description = "음식 종류 ID") @RequestParam(required = false) Long foodTypeId,
            @Parameter(description = "사용자 위도 (선택)") @RequestParam(required = false) Double lat,
            @Parameter(description = "사용자 경도 (선택)") @RequestParam(required = false) Double lng,
            @Parameter(description = "정렬: distance / jjinScore / score") @RequestParam(defaultValue = "distance") String sort,
            @Parameter(description = "페이지 (1부터)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        return restaurantSearchService.search(
                keyword, region, foodTypeId, lat, lng, sort, page, size
        );
    }
}
