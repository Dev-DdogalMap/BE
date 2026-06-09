package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.response.UserStatsResponse;

public interface UserStatsService {
	UserStatsResponse getMyStats(Long userId);
}
