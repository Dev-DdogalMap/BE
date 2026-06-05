package com.ddogalmap.domain.bookmarks.dto.request;

// 카테고리에 가게 즐겨찾기
public record CreateBookmarkRequest(
        Long restaurantId,
        Long bookmarkCategoryId,
        String memo
) {
}