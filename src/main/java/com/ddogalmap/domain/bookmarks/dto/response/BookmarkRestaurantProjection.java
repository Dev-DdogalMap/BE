package com.ddogalmap.domain.bookmarks.dto.response;

import java.time.LocalDateTime;

public interface BookmarkRestaurantProjection {
    Long getBookmarkId();

    Long getRestaurantId();

    String getRestaurantName();

    String getCategory();

    String getAddress();

    String getImageUrl();

    String getMemo();

    LocalDateTime getCreatedAt();

    Double getAverageScore();

    Long getReviewCount();

    String getTopTags();
}