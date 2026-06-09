package com.ddogalmap.domain.bookmarks.dto.response;

//저장됨 누를 때 모달 카테고리들 응답
public record BookmarkCategoryStatusResponse(
        Long bookmarkCategoryId,
        String bookmarkCategoryName,
        Integer sortOrder,
        Boolean isDefault,
        Long bookmarkCount,
        Boolean saved,
        Long bookmarkId
) {
}