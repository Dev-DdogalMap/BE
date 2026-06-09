package com.ddogalmap.domain.bookmarks.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookmarkSortType {
    LATEST("최신 순"),
    RATING("별점 높은 순"),
    REVIEW_COUNT("리뷰 많은 순"),
    FOOD_SCORE("찐맛집지수 순");

    private final String label; // 화면에 보여줄 라벨
}