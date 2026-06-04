package com.ddogalmap.global.security.jwt;

import com.ddogalmap.domain.users.enumtype.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";
    private static final String USER_ID_CLAIM = "userId";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, UserRole role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirationMs);

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(USER_ID_CLAIM, userId)
                .claim(ROLE_CLAIM, role.name())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();

        log.info("Access Token 발급 userId={}, role={}", userId, role);

        return token;
    }

    public String createRefreshToken(Long userId, UserRole role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpirationMs);

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(USER_ID_CLAIM, userId)
                .claim(ROLE_CLAIM, role.name())
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();

        log.info("Refresh Token 발급 userId={}, role={}", userId, role);

        return token;
    }

    public Long getUserId(String token) {
        try {
            Claims claims = parseClaims(token);
            String subject = claims.getSubject();

            // 문자열 "null"로 들어오거나 진짜 null인 경우 분기 처리
            if (subject == null || subject.isBlank() || "null".equals(subject)) {
                return null;
            }
            return Long.valueOf(subject);
        } catch (Exception e) {
            return null; // 파싱 중 에러 발생 시 예외를 던지지 않고 null 반환
        }
    }

    public UserRole getRole(String token) {
        Claims claims = parseClaims(token);
        return UserRole.valueOf(claims.get(ROLE_CLAIM, String.class));
    }

    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get(TOKEN_TYPE_CLAIM, String.class);
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}