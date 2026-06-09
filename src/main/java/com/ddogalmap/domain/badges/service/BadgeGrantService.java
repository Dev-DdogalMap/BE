package com.ddogalmap.domain.badges.service;

public interface BadgeGrantService {
    void checkReviewBadges(Long userId, Long reviewId);
    void checkVisitVerificationBadge(Long userId);
    void checkChatRequestReceivedBadge(Long userId);
    void grantNewUserBadge(Long userId);
}
