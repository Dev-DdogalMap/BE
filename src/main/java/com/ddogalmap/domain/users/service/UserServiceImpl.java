package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.response.MyProfileResponse;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public MyProfileResponse getMyProfile(Long userId) {
        return userRepository.findById(userId)
                .map(MyProfileResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
