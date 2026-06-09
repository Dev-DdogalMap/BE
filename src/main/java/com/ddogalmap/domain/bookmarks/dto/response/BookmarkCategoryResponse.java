package com.ddogalmap.domain.bookmarks.dto.response;

public record BookmarkCategoryResponse(
        Long bookmarkCategoryId,
        String bookmarkCategoryName,
        Integer sortOrder,
        Boolean isDefault,
        Long bookmarkCount
) {
}