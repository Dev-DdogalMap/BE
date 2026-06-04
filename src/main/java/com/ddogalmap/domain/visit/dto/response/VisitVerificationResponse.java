package com.ddogalmap.domain.visit.dto.response;

import java.time.LocalDateTime;

public record VisitVerificationResponse(
        Long visitVerificationId,
        Long restaurantId,
        Double distanceMeter,
        LocalDateTime verifiedAt
) {
}