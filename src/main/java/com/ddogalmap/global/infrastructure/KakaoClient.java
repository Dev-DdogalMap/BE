package com.ddogalmap.global.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoClient {

    @Value("${kakao.admin-key}")
    private String adminKey;

    private final RestClient restClient = RestClient.create();

    public void unlink(Long kakaoId) {
        if (kakaoId == null) {
            return;
        }

        try {
            restClient.post()
                    .uri("https://kapi.kakao.com/v1/user/unlink")
                    .header("Authorization", "KakaoAK " + adminKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("target_id_type=user_id&target_id=" + kakaoId)
                    .retrieve()
                    .toBodilessEntity();

            log.info("카카오 연결 끊기 성공 kakaoId={}", kakaoId);

        } catch (HttpClientErrorException.BadRequest e) {
            String body = e.getResponseBodyAsString();

            if (body.contains("NotRegisteredUserException")
                    || body.contains("\"code\":-101")) {
                log.warn("카카오 연결이 이미 끊겼거나 해당 앱에 연결되지 않은 사용자입니다. kakaoId={}", kakaoId);
                return;
            }

            throw e;
        }
    }
}