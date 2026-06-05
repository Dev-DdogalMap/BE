package com.ddogalmap.domain.levels.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {

    LOGIN("로그인"),
    LOCAL_AUTH_COMPLETE("내 동네 인증 완료"),
    VISIT_VERIFY("방문 인증"),
    REVIEW_WRITE("리뷰 작성"),
    REVIEW_PHOTO("사진 포함 리뷰 작성"),
    RESTAURANT_BOOKMARK("맛집 북마크"),
    GROUP_CHAT_JOIN("그룹 채팅방 참여"),
    CHAT_RESPONSE("찐맛집러 채팅 응답");
    private final String description;
}