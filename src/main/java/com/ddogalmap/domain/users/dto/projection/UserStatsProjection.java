package com.ddogalmap.domain.users.dto.projection;

public interface UserStatsProjection {
	Long getVisitCount();
	Long getReviewCount();
	Long getBookmarkCount();
	Long getChatRoomCount();
}