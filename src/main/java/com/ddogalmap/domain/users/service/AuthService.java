package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.bookmarks.entity.BookmarkCategory;
import com.ddogalmap.domain.bookmarks.repository.BookmarkCategoryRepository;
import com.ddogalmap.domain.levels.dto.LevelExpEvent;
import com.ddogalmap.domain.levels.entity.Level;
import com.ddogalmap.domain.levels.entity.UserLevel;
import com.ddogalmap.domain.levels.enumtype.ActivityType;
import com.ddogalmap.domain.levels.repository.LevelRepository;
import com.ddogalmap.domain.levels.repository.UserLevelRepository;
import com.ddogalmap.domain.users.dto.response.AccessTokenResponse;
import com.ddogalmap.domain.users.dto.response.LoginTokenResult;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient.Builder webClientBuilder;
    private final UserLevelRepository userLevelRepository;
    private final LevelRepository levelRepository;
    private final ApplicationEventPublisher eventPublisher;

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

    private final BookmarkCategoryRepository bookmarkCategoryRepository;

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
    public LoginTokenResult kakaoLogin(String code) {
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
                .orElseGet(() -> {
                    User newUser = User.createKakaoUser(kakaoId, email, nickname, profileImageUrl);

                    User savedUser = userRepository.save(newUser);

                    bookmarkCategoryRepository.save(
                            BookmarkCategory.createDefault(savedUser)
                    );

                    return savedUser;
                });


        createUserLevelIfNotExists(user); // 사용자 레벨 생성

        // 경험치 이벤트 발행 - 1시간에 1번
        Long loginReferenceId = Long.valueOf(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"))
        );

        eventPublisher.publishEvent(
                new LevelExpEvent(user.getUserId(), ActivityType.LOGIN, loginReferenceId)
        );

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getRole()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getUserId(),
                user.getRole()
        );

        return new LoginTokenResult(
                accessToken,
                refreshToken,
                user.getUserId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    @Transactional(readOnly = true)
    public AccessTokenResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token 타입이 아닙니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getRole()
        );

        return new AccessTokenResponse(
                newAccessToken,
                user.getUserId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        // 지금은 refreshToken을 DB/Redis에 저장하지 않는 구조라서
        // 서버에서 삭제할 데이터는 없음.
        // 나중에 refreshToken을 Redis나 DB에 저장하면 여기서 삭제 처리하면 됨.
    }

    private void createUserLevelIfNotExists(User user) {
        if (userLevelRepository.existsByUserUserId(user.getUserId())) {
            return;
        }

        try {
            createInitialUserLevel(user);
        } catch (DataIntegrityViolationException e) {
            log.info("[AuthService] UserLevel already created by concurrent login. userId={}",
                    user.getUserId());
        }
    }

    private void createInitialUserLevel(User user) {
        Level level1 = levelRepository.findByLevel(1)
                .orElseThrow(() -> new IllegalStateException("기본 레벨을 찾을 수 없습니다."));

        userLevelRepository.save(
                UserLevel.create(user, level1, 0)
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