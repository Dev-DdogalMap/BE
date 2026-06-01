package com.ddogalmap.domain.users.controller;

import com.ddogalmap.domain.users.dto.response.LoginResponse;
import com.ddogalmap.domain.users.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

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
            description = "카카오 인가 코드를 받아 로그인 처리 후 JWT를 쿠키에 저장하고 프론트 성공 페이지로 리다이렉트합니다."
    )
    @GetMapping("/kakao/callback")
    public void kakaoCallback(
            @RequestParam String code,
            HttpServletResponse response
    ) throws IOException {
        LoginResponse loginResponse = authService.kakaoLogin(code);

        Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.accessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // 로컬 http에서는 false
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60); // 1시간

        response.addCookie(accessTokenCookie);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/oauth/success")
                .build()
                .toUriString();

        log.info("frontendUrl={}", frontendUrl);
        log.info("redirectUrl={}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    @Operation(summary = "로그아웃", description = "Access Token 쿠키를 삭제합니다.")
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}