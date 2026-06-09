package com.ddogalmap.domain.bookmarks.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BookmarkRestaurantResponse(
        Long bookmarkId,
        Long restaurantId,
        String restaurantName,
        String category,
        String address,
        String imageUrl,
        String memo,
        LocalDateTime createdAt,

        Double averageScore,
        Long reviewCount,
        Double foodScore,
        List<String> topTags
) {
}