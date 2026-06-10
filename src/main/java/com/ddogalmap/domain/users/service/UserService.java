package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.response.MyProfileResponse;

public interface UserService {
    MyProfileResponse getMyProfile(Long userId);
}
