package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.request.RepresentativeBadgeUpdateRequest;
import com.ddogalmap.domain.users.dto.response.ActivityDetailResponse;
import com.ddogalmap.domain.users.dto.response.ActivityResponse;
import com.ddogalmap.domain.badges.dto.response.BadgeResponse;

public interface UserActivityService {
    ActivityResponse getMyActivity(Long userId);

    BadgeResponse updateRepresentativeBadge(Long userId, RepresentativeBadgeUpdateRequest request);

    ActivityDetailResponse getMyActivityDetail(Long userId);
}
