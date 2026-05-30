package com.ddogalmap.domain.restaurants.controller;

import com.ddogalmap.domain.restaurants.dto.response.RestaurantMapResponse;
import com.ddogalmap.domain.restaurants.dto.response.RestaurantPreviewResponse;
import com.ddogalmap.domain.restaurants.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Restaurant", description = "맛집 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Operation(
            summary = "음식점 map 조회 API",
            description = "음식점의 위치 정보를 조회합니다. (최소 1건, 최대 500건)"
    )
    @GetMapping("/map")
    public ResponseEntity<RestaurantMapResponse> getRestaurantsOnMap(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double swLat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double swLng,
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double neLat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double neLng,
            @RequestParam(defaultValue = "300") @Min(1) @Max(500) int limit
    ) {
        return ResponseEntity.ok(
                restaurantService.getRestaurantsOnMap(swLat, swLng, neLat, neLng, limit)
        );
    }

    @Operation(
            summary = "음식점 미리보기 조회 API",
            description = "음식점의 기본 정보를 조회합니다."
    )
    @GetMapping("/{restaurantId}/preview")
    public ResponseEntity<RestaurantPreviewResponse> getRestaurantPreview(
            @PathVariable Long restaurantId, @RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(
                restaurantService.getRestaurantPreview(restaurantId, lat, lng)
        );
    }
}
