package com.ddogalmap.domain.users.controller;

import com.ddogalmap.domain.users.dto.response.AccessTokenResponse;
import com.ddogalmap.domain.users.dto.response.LoginTokenResult;
import com.ddogalmap.domain.users.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

@Slf4j
@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthService authService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Operation(
            summary = "카카오 로그인 시작",
            description = "카카오 로그인 페이지로 리다이렉트합니다."
    )
    @GetMapping("/kakao/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String kakaoLoginUrl = authService.getKakaoLoginUrl();
        response.sendRedirect(kakaoLoginUrl);
    }

    @Operation(
            summary = "카카오 로그인 콜백",
            description = "카카오 인가 코드를 받아 로그인 처리 후 Refresh Token을 쿠키에 저장하고 프론트 성공 페이지로 리다이렉트합니다."
    )
    @GetMapping("/kakao/callback")
    public void kakaoCallback(
            @RequestParam String code,
            HttpServletResponse response
    ) throws IOException {
        LoginTokenResult loginResult = authService.kakaoLogin(code);

        ResponseCookie refreshTokenCookie = ResponseCookie.from(
                        REFRESH_TOKEN_COOKIE_NAME,
                        loginResult.refreshToken()
                )
                .httpOnly(true)
                .secure(isSecureCookie())
                .sameSite(cookieSameSite())
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(14))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/oauth/success")
                .build()
                .toUriString();

        log.info("frontendUrl={}", frontendUrl);
        log.info("redirectUrl={}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    @Operation(
            summary = "Access Token 재발급",
            description = "Refresh Token 쿠키를 검증한 뒤 새로운 Access Token을 응답 body로 내려줍니다."
    )
    @PostMapping("/refresh")
    public AccessTokenResponse refresh(HttpServletRequest request) {
        String refreshToken = resolveRefreshToken(request);
        return authService.refreshAccessToken(refreshToken);
    }

    @Operation(summary = "로그아웃", description = "Refresh Token 쿠키를 삭제합니다.")
    @PostMapping("/logout")
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = resolveRefreshToken(request);

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie deleteCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isSecureCookie())
                .sameSite(cookieSameSite())
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean isSecureCookie() {
        return !frontendUrl.startsWith("http://localhost");
    }

    private String cookieSameSite() {
        return isSecureCookie() ? "None" : "Lax";
    }
}