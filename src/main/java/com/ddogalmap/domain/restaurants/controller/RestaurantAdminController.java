package com.ddogalmap.domain.restaurants.controller;

import com.ddogalmap.domain.restaurants.dto.ImportResult;
import com.ddogalmap.domain.restaurants.service.RestaurantImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Restaurant Admin", description = "음식점 데이터 적재 (관리용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/restaurants")
public class RestaurantAdminController {

    private final RestaurantImportService importService;

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
}
