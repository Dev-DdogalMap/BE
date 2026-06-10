package com.ddogalmap.domain.users.controller;

import com.ddogalmap.domain.badges.dto.response.BadgeResponse;
import com.ddogalmap.domain.users.dto.request.ChatPreferenceUpdateRequest;
import com.ddogalmap.domain.users.dto.request.RegionVerificationRequest;
import com.ddogalmap.domain.users.dto.request.RepresentativeBadgeUpdateRequest;
import com.ddogalmap.domain.users.dto.response.*;
import com.ddogalmap.domain.users.service.*;
import com.ddogalmap.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class UserController {

	private final RegionVerificationService regionVerificationService;
	private final UserPreferenceService userPreferenceService;
	private final UserWithdrawalService userWithdrawalService;
	private final UserActivityService userActivityService;
	private final UserStatsService userStatsService;
	private final UserService userService;

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

	@Operation(
			summary = "내 채팅 수신 설정 조회",
			description = "현재 로그인한 사용자의 1:1 채팅 수신 허용 여부를 조회합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping("/me/chat-preference")
	public ChatPreferenceResponse getChatPreference(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		return userPreferenceService.getChatPreference(principal.userId());
	}

	@Operation(
			summary = "내 채팅 수신 설정 변경",
			description = "현재 로그인한 사용자의 1:1 채팅 수신 허용 여부를 변경합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@PatchMapping("/me/chat-preference")
	public ChatPreferenceResponse updateChatPreference(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestBody ChatPreferenceUpdateRequest request
	) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		return userPreferenceService.updateChatPreference(principal.userId(), request);
	}

	@Operation(
			summary = "내 동네 인증",
			description = "사용자의 경도 위도 값을 확인하여 내 동네를 인증합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@PostMapping("/me/region-verification")
	public ResponseEntity<RegionVerificationResponse> verifyRegion(
			@AuthenticationPrincipal UserPrincipal user,
			@RequestBody RegionVerificationRequest request
	) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		return ResponseEntity.ok(
				regionVerificationService.verifyRegion(user.userId(), request)
		);
	}

	@Operation(
			summary = "내 동네 인증 정보 조회",
			description = "현재 로그인한 사용자의 동네 인증 정보를 조회합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping("/me/region-verification")
	public ResponseEntity<RegionVerificationStatusResponse> getRegionVerification(
			@AuthenticationPrincipal UserPrincipal user
	) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		return ResponseEntity.ok(
				regionVerificationService.getRegionVerification(user.userId())
		);
	}

	@Operation(
			summary = "회원 탈퇴",
			description = """
                    현재 로그인한 사용자의 카카오 연결을 끊고,
                    users 테이블의 개인정보를 익명화합니다.

                    리뷰, 북마크, 채팅 등 기존 서비스 데이터는 삭제하지 않습니다.
                    """,
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@DeleteMapping("/me")
	public ResponseEntity<Void> withdraw(
			@AuthenticationPrincipal UserPrincipal userPrincipal,
			HttpServletResponse response
	) {
		if (userPrincipal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		userWithdrawalService.withdraw(userPrincipal.userId());

		ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.path("/")
				.maxAge(0)
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString());

		return ResponseEntity.noContent().build();
	}

	@Operation(
			summary = "내 활동 내역 조회",
			description = "현재 로그인한 사용자의 레벨 정보, 대표 뱃지, 최근 획득한 뱃지 3개를 조회합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping("/me/activity")
	public ResponseEntity<ActivityResponse> getMyActivity(
			@AuthenticationPrincipal UserPrincipal user
	) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		return ResponseEntity.ok(
				userActivityService.getMyActivity(user.userId())
		);
	}

	@Operation(
			summary = "내 활동 내역 상세 조회",
			description = "현재 로그인한 사용자의 레벨 정보, 대표 뱃지, 전체 뱃지 목록, 최근 레벨 히스토리 10개를 조회합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping("/me/activity/detail")
	public ResponseEntity<ActivityDetailResponse> getMyActivityDetail(
			@AuthenticationPrincipal UserPrincipal user
	) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		return ResponseEntity.ok(
				userActivityService.getMyActivityDetail(user.userId())
		);
	}

	@Operation(
			summary = "대표 뱃지 변경",
			description = "사용자의 대표 뱃지를 변경합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@PatchMapping("/me/representative-badge")
	public ResponseEntity<BadgeResponse> updateRepresentativeBadge(
			@AuthenticationPrincipal UserPrincipal user,
			@Valid @RequestBody RepresentativeBadgeUpdateRequest request
	) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		return ResponseEntity.ok(
				userActivityService.updateRepresentativeBadge(
						user.userId(),
						request
				)
		);
	}

	@Operation(
			summary = "내 전체 활동 수 조회",
			description = """
                마이페이지에 표시되는 로그인 사용자의 활동 통계를 조회합니다.
                - 조회 항목: 방문 인증 수, 작성한 후기 수, 북마크 수, 참여 중인 채팅방 수
                """
	)
	@GetMapping("/me/stats")
	public UserStatsResponse getMyStats(@AuthenticationPrincipal UserPrincipal user) {
		return userStatsService.getMyStats(user.userId());
	}

	@Operation(
			summary = "내 정보 조회",
			description = "사용자의 정보(이메일, 닉네임, 프로필이미지)를 조회합니다.",
			security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping("/me/profile")
	public ResponseEntity<MyProfileResponse> getMyProfile(
			@AuthenticationPrincipal UserPrincipal user
	) {
		return ResponseEntity.ok(
				userService.getMyProfile(user.userId())
		);
	}

}