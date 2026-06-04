package com.ddogalmap.domain.visit.controller;

import com.ddogalmap.domain.visit.dto.request.VisitVerificationRequest;
import com.ddogalmap.domain.visit.dto.response.VisitVerificationResponse;
import com.ddogalmap.domain.visit.service.VisitVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Visit Verification", description = "방문 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visit")
public class VisitVerificationController {

    private final VisitVerificationService visitVerificationService;

    @Operation(
            summary = "방문 인증",
            description = """
                    사용자의 현재 위치와 선택한 음식점 위치를 비교하여 방문 인증을 수행합니다.
                    
                    - 인증된 사용자만 요청할 수 있습니다.
                    - 프론트에서 전달한 사용자 현재 좌표와 DB에 저장된 음식점 좌표를 비교합니다.
                    - 음식점 기준 반경 50m 이내인 경우 방문 인증 기록을 저장합니다.
                    - 프론트의 거리 계산 결과는 신뢰하지 않고, 서버에서 다시 거리 검증을 수행합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "방문 인증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VisitVerificationResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "visitVerificationId": 1,
                                              "restaurantId": 104242,
                                              "distanceMeter": 12.5,
                                              "verifiedAt": "2026-06-03T16:20:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "방문 인증 실패 - 음식점 반경 50m 밖 또는 잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "가게 반경 50m 이내에서만 방문 인증할 수 있습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "음식점을 찾을 수 없음"
            )
    })
    @PostMapping("/visit-verification")
    public ResponseEntity<VisitVerificationResponse> verifyVisit(
            @Parameter(hidden = true)
            Authentication authentication,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "방문 인증 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = VisitVerificationRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "restaurantId": 104242,
                                              "userLatitude": 37.5665,
                                              "userLongitude": 126.9780,
                                              "accuracyMeter": 15.2
                                            }
                                            """
                            )
                    )
            )
            @RequestBody VisitVerificationRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        VisitVerificationResponse response =
                visitVerificationService.verifyVisit(userId, request);

        return ResponseEntity.ok(response);
    }
}