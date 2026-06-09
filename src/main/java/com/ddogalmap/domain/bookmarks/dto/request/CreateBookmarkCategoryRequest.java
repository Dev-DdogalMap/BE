package com.ddogalmap.domain.bookmarks.dto.request;

// 새 카테고리 추가
public record CreateBookmarkCategoryRequest(
        String bookmarkCategoryName
) {
}