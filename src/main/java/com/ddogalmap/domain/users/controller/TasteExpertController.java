package com.ddogalmap.domain.users.controller;

import com.ddogalmap.domain.users.dto.response.TasteExpertPageResponse;
import com.ddogalmap.domain.users.service.TasteExpertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Taste Expert", description = "맛잘알 조회 API")
@RestController
@RequestMapping("/api/taste-experts")
@RequiredArgsConstructor
public class TasteExpertController {

    private final TasteExpertService tasteExpertService;

    @Operation(
            summary = "맛잘알 목록 조회",
            description = "검색어, 지역, 최소 레벨, 정렬, 페이지 조건으로 맛잘알 유저 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public TasteExpertPageResponse getTasteExperts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(required = false, defaultValue = "EXPERTISE") String sort,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return tasteExpertService.getTasteExperts(keyword, region, minLevel, sort, page, size);
    }
}
