package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import com.ddogalmap.global.infrastructure.KakaoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWithdrawalService {

    private final UserRepository userRepository;
    private final KakaoClient kakaoClient;

    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Long kakaoId = user.getKakaoId();

        if (kakaoId != null) {
            kakaoClient.unlink(kakaoId);
        }

        user.withdraw();
    }
}