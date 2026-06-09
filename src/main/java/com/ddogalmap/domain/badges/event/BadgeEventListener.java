package com.ddogalmap.domain.badges.event;

import com.ddogalmap.domain.badges.dto.ChatRequestReceivedEvent;
import com.ddogalmap.domain.badges.dto.NewUserCreatedEvent;
import com.ddogalmap.domain.badges.dto.ReviewCreatedEvent;
import com.ddogalmap.domain.badges.dto.VisitVerifiedEvent;
import com.ddogalmap.domain.badges.service.BadgeGrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BadgeEventListener {

    private final BadgeGrantService badgeGrantService;

    /**
     * 리뷰 작성 후 리뷰/음식 카테고리 뱃지 검사
     */
    @Async("badgeEventExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        badgeGrantService.checkReviewBadges(
                event.userId(),
                event.reviewId()
        );
    }

    /**
     * 방문 인증 후 방문 인증 뱃지 검사
     */
    @Async("badgeEventExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleVisitVerifiedEvent(VisitVerifiedEvent event) {
        badgeGrantService.checkVisitVerificationBadge(event.userId());
    }

    /**
     * 채팅 요청 수신 후 채팅 뱃지 검사
     */
    @Async("badgeEventExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleChatRequestReceivedEvent(ChatRequestReceivedEvent event) {
        badgeGrantService.checkChatRequestReceivedBadge(event.receiverId());
    }

    @Async("badgeEventExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleNewUserCreatedEvent(NewUserCreatedEvent event) {
        badgeGrantService.grantNewUserBadge(event.userId());
    }
}
