package com.ddogalmap.domain.users.dto.response;

import java.time.LocalDateTime;

public record RegionVerificationStatusResponse( // 내 동네 인증 MyNeighborhoodSection연결
												String eupmyeondongName, // users.region
												boolean verified,
												LocalDateTime verifiedAt
) {
}