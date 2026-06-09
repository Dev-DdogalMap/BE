package com.ddogalmap.domain.bookmarks.service;

import com.ddogalmap.domain.bookmarks.dto.request.CreateBookmarkCategoryRequest;
import com.ddogalmap.domain.bookmarks.dto.request.CreateBookmarkRequest;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkCategoryResponse;
import com.ddogalmap.domain.bookmarks.dto.response.CreateBookmarkResponse;

//변경 전용 CUD
public interface BookmarkCommandService {

    CreateBookmarkResponse createBookmark(Long userId, CreateBookmarkRequest request);

    void deleteBookmarkFromCategory(
            Long userId,
            Long bookmarkCategoryId,
            Long restaurantId
    );

    BookmarkCategoryResponse createBookmarkCategory(
            Long userId,
            CreateBookmarkCategoryRequest request
    );

    void deleteBookmarkCategory(Long userId, Long bookmarkCategoryId);
}
