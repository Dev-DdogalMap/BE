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

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public UserRole getRole(String token) {
        try {
            Claims claims = parseClaims(token);
            String roleStr = claims.get("role", String.class);

            if (roleStr == null || roleStr.isBlank()) {
                return null;
            }
            return UserRole.valueOf(roleStr);
        } catch (Exception e) {
            return null;
        }
    }

    public String createAccessToken(Long userId, UserRole role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirationMs);

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();

        log.info("Access Token 발급 userId={}, role={}", userId, role);
        log.info("Access Token={}", token);

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

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
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