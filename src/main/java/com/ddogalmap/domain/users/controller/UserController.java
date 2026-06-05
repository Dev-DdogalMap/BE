package com.ddogalmap.domain.users.controller;

import com.ddogalmap.domain.users.dto.request.RegionVerificationRequest;
import com.ddogalmap.domain.users.dto.response.RegionVerificationResponse;
import com.ddogalmap.domain.users.service.RegionVerificationService;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@SecurityRequirement(name = "BearerAuth")
@Slf4j
public class UserController {

	private final RegionVerificationService regionVerificationService;

	@Operation(
			summary = "내 정보 조회",
			description = """
					현재 로그인한 사용자의 정보를 조회합니다.
					
					Authorization 헤더에 Bearer JWT Access Token을 포함해야 합니다.
					""",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping("/me")
	public Map<String, Object> me(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		return Map.of(
				"userId", principal.userId()
		);
	}


	@PostMapping("/me/region-verification")
	public ResponseEntity<RegionVerificationResponse> verifyRegion(@AuthenticationPrincipal UserPrincipal user,
																   @RequestBody RegionVerificationRequest request) {

		return ResponseEntity.ok(regionVerificationService.verifyRegion(user.userId(), request));
	}

}