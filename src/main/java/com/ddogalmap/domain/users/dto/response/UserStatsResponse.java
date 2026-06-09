package com.ddogalmap.domain.users.dto.response;

import com.ddogalmap.domain.users.dto.projection.UserStatsProjection;

public record UserStatsResponse(
		Long visitCount,
		Long reviewCount,
		Long bookmarkCount,
		Long chatRoomCount
) {
	public static UserStatsResponse from(UserStatsProjection p) {
		return new UserStatsResponse(
				p.getVisitCount(),
				p.getReviewCount(),
				p.getBookmarkCount(),
				p.getChatRoomCount()
		);
	}
}