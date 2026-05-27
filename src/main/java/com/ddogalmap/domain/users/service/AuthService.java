package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.response.LoginResponse;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient.Builder webClientBuilder;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.auth-uri}")
    private String kakaoAuthUri;

    @Value("${kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    public String getKakaoLoginUrl() {
        return UriComponentsBuilder
                .fromUriString(kakaoAuthUri)
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", kakaoRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("prompt", "login")
                .build()
                .toUriString();
    }

    @Transactional
    public LoginResponse kakaoLogin(String code) {
        String kakaoAccessToken = getKakaoAccessToken(code);
        Map<String, Object> kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);

        Long kakaoId = ((Number) kakaoUserInfo.get("id")).longValue();

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) kakaoUserInfo.get("kakao_account");

        Map<String, Object> profile = kakaoAccount == null
                ? null
                : (Map<String, Object>) kakaoAccount.get("profile");

        String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");
        String nickname = profile == null ? "카카오사용자" : (String) profile.get("nickname");
        String profileImageUrl = profile == null ? null : (String) profile.get("profile_image_url");

        User user = userRepository.findByKakaoId(kakaoId)
                .map(existingUser -> {
                    existingUser.updateKakaoProfile(email, nickname, profileImageUrl);
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(
                        User.createKakaoUser(kakaoId, email, nickname, profileImageUrl)
                ));

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole());

        return new LoginResponse(
                accessToken,
                user.getUserId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    private String getKakaoAccessToken(String code) {
        Map<String, Object> response = webClientBuilder.build()
                .post()
                .uri(kakaoTokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("grant_type", "authorization_code")
                        .with("client_id", kakaoClientId)
                        .with("client_secret", kakaoClientSecret)
                        .with("redirect_uri", kakaoRedirectUri)
                        .with("code", code)
                )
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("카카오 Access Token 발급에 실패했습니다.");
        }

        return (String) response.get("access_token");
    }

    private Map<String, Object> getKakaoUserInfo(String kakaoAccessToken) {
        Map<String, Object> response = webClientBuilder.build()
                .get()
                .uri(kakaoUserInfoUri)
                .headers(headers -> headers.setBearerAuth(kakaoAccessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            throw new IllegalStateException("카카오 사용자 정보 조회에 실패했습니다.");
        }

        return response;
    }
}