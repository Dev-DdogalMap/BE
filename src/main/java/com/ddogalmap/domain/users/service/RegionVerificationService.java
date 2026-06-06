package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.request.RegionVerificationRequest;
import com.ddogalmap.domain.users.dto.response.RegionVerificationResponse;
import com.ddogalmap.domain.users.dto.response.RegionVerificationStatusResponse;

public interface RegionVerificationService {

	RegionVerificationStatusResponse getRegionVerification(Long userId);
	RegionVerificationResponse verifyRegion(Long userId, RegionVerificationRequest request);

}