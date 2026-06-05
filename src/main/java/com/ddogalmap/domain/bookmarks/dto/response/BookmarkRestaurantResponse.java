package com.ddogalmap.domain.bookmarks.dto.response;

import java.time.LocalDateTime;

public record BookmarkRestaurantResponse(
        Long bookmarkId,
        Long restaurantId,
        String restaurantName,
        String category,
        String address,
        String imageUrl,
        String memo,
        LocalDateTime createdAt
) {
}