package com.ddogalmap.domain.visit.dto.request;

public record VisitVerificationRequest(
        Long restaurantId,
        Double userLatitude,
        Double userLongitude,
        Double accuracyMeter
) {
}