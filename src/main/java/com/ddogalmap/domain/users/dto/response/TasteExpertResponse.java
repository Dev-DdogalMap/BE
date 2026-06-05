package com.ddogalmap.domain.users.dto.response;

public record TasteExpertResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String region,
        Integer level,
        String levelName,
        Long exp,
        Long reviewCount,
        Long visitVerificationCount,
        Double ratingAverage,
        String specialty,
        boolean isCertified
) {
}
