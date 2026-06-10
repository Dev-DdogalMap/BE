package com.ddogalmap.domain.reviews.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.ddogalmap.domain.visit.entity.VisitVerification;

public record UnwrittenReviewResponseDto(
        Long visitVerificationId, // React Key 중복 에러 해결용 필드 추가
        Long restaurantId,
        String restaurantName,
        String category,
        String address,
        String visitDate,
        long daysRemaining // 삭제까지 남은 일수
) {
    public static UnwrittenReviewResponseDto from(VisitVerification vv) {

        // 💡 현재 시간과 '방문 인증 시간 + 7일' 사이의 남은 일수 계산
        long remaining = ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                vv.getVerifiedAt().plusDays(7)
        );

        // 음수 값이 나오지 않도록 최소값 0 처리 (이미 7일이 지난 경우)
        long daysRemaining = Math.max(0, remaining);

        return new UnwrittenReviewResponseDto(
                vv.getId(),
                vv.getRestaurant().getRestaurantId(),
                vv.getRestaurant().getPlaceName(),
                vv.getRestaurant().getFoodType().getType(),
                vv.getRestaurant().getAddressName(),
                vv.getVerifiedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                daysRemaining // 💡 계산된 남은 일수 주입
        );
    }
}