package com.ddogalmap.domain.reviews.repository;

import com.ddogalmap.domain.reviews.entity.Review;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.ddogalmap.domain.reviews.entity.QReview.review;

@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Review> findReviewsWithFilter(Long restaurantId, boolean hasImage, Pageable pageable) {
        int pageSize = pageable.getPageSize();

        // 1. 데이터 목록 조회 (요청된 크기보다 1개 더 많이 조회)
        List<Review> content = queryFactory
                .selectFrom(review)
                .where(
                        review.restaurantId.eq(restaurantId),
                        hasImageEq(hasImage)
                )
                .orderBy(getOrderSpecifier(pageable.getSort())) // 💡 변경: 동적 정렬 적용
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .distinct()
                .fetch();

        // 2. 다음 페이지 존재 여부 확인
        boolean hasNext = false;
        if (content.size() > pageSize) {
            content.remove(pageSize);
            hasNext = true;
        }

        // 3. SliceImpl 객체 반환
        return new SliceImpl<>(content, pageable, hasNext);
    }

    // 💡 Pageable의 Sort 정보를 기반으로 Querydsl의 정렬 객체를 생성하는 헬퍼 메서드
    private OrderSpecifier<?> getOrderSpecifier(Sort sort) {
        // 기본값 지정 (정렬 정보가 없을 경우 최신순)
        OrderSpecifier<?> orderSpecifier = review.createdAt.desc();

        if (sort != null && sort.isSorted()) {
            for (Sort.Order order : sort) {
                // 프론트에서 보낸 정렬 방향 결정 (ASC / DESC)
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                // 정렬 타겟 필드 식별
                if ("createdAt".equals(order.getProperty())) {
                    orderSpecifier = new OrderSpecifier<>(direction, review.createdAt);
                } else if ("score".equals(order.getProperty())) { // 추후 별점순 정렬이 추가될 경우를 대비
                    orderSpecifier = new OrderSpecifier<>(direction, review.score);
                }
            }
        }
        return orderSpecifier;
    }

    private BooleanExpression hasImageEq(boolean hasImage) {
        return hasImage ? review.images.isNotEmpty() : null;
    }
}