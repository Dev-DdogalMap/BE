package com.ddogalmap.domain.reviews.dto.response;

import java.time.format.DateTimeFormatter;
import com.ddogalmap.domain.visit.entity.VisitVerification;

public record UnwrittenReviewResponseDto(
        Long visitVerificationId, // React Key 중복 에러 해결용 필드 추가
        Long restaurantId,
        String restaurantName,
        String category,
        String address,
        String visitDate
) {
    public static UnwrittenReviewResponseDto from(VisitVerification vv) {
        return new UnwrittenReviewResponseDto(
                vv.getId(),
                vv.getRestaurant().getRestaurantId(),
                vv.getRestaurant().getPlaceName(),

                vv.getRestaurant().getFoodType().getType(),

                vv.getRestaurant().getAddressName(),
                vv.getVerifiedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
    }
}