package com.ddogalmap.domain.regions.controller;

import com.ddogalmap.domain.regions.dto.response.RegionTreeResponse;
import com.ddogalmap.domain.regions.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @Operation(
            summary = "지역 트리 조회",
            description = """
                시도 → 시군구 → 읍면동 구조의 지역 목록을 조회합니다.
                """
    )
    @GetMapping("/tree")
    public List<RegionTreeResponse> getRegionTree() {
        return regionService.getRegionTree();
    }
}
