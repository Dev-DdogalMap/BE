package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.request.RegionVerificationRequest;
import com.ddogalmap.domain.users.dto.response.RegionVerificationResponse;

public interface RegionVerificationService {

	RegionVerificationResponse verifyRegion(Long userId, RegionVerificationRequest request);

}