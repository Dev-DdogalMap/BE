package com.ddogalmap.domain.users.dto.request;

public record RegionVerificationRequest(
		Double latitude,
		Double longitude,
		Double accuracy
) {
}