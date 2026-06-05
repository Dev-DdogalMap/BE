package com.ddogalmap.domain.users.dto.response;

import java.time.LocalDateTime;

public record RegionVerificationResponse(
		String eupmyeondongName,
		boolean verified,
		LocalDateTime verifiedAt
) {
}