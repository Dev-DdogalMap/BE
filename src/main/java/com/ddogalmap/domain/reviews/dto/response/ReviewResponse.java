package com.ddogalmap.domain.reviews.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
    Long reviewId,
    String nickname,
    Integer score,
    String content,
    boolean isRevisit,
    LocalDateTime createdAt,
    List<String> imageUrls,
    List<String> tags,
    int likeCount,
    String restaurantName
)   {
}
