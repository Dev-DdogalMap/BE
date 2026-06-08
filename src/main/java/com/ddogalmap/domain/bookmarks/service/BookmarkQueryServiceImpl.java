package com.ddogalmap.domain.bookmarks.service;

import com.ddogalmap.domain.bookmarks.dto.response.BookmarkCategoryResponse;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkCategoryStatusResponse;
import com.ddogalmap.domain.bookmarks.dto.projection.BookmarkRestaurantProjection;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkRestaurantResponse;
import com.ddogalmap.domain.bookmarks.entity.Bookmark;
import com.ddogalmap.domain.bookmarks.entity.BookmarkCategory;
import com.ddogalmap.domain.bookmarks.repository.BookmarkCategoryRepository;
import com.ddogalmap.domain.bookmarks.repository.BookmarkRepository;
import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.restaurants.repository.RestaurantRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkQueryServiceImpl implements BookmarkQueryService {

    private final UserRepository userRepository;
    private final BookmarkCategoryRepository bookmarkCategoryRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public List<BookmarkCategoryResponse> getMyBookmarkCategories(Long userId) {
        User user = getUser(userId);

        return bookmarkCategoryRepository.findAllByUserOrderBySortOrderAscBookmarkCategoryIdAsc(user)
                .stream()
                .map(category -> new BookmarkCategoryResponse(
                        category.getBookmarkCategoryId(),
                        category.getBookmarkCategoryName(),
                        category.getSortOrder(),
                        category.getIsDefault(),
                        bookmarkRepository.countByBookmarkCategory(category)
                ))
                .toList();
    }

    @Override
    public List<BookmarkRestaurantResponse> getMyBookmarksByCategory(
            Long userId,
            Long bookmarkCategoryId
    ) {
        User user = getUser(userId);

        bookmarkCategoryRepository.findByBookmarkCategoryIdAndUser(bookmarkCategoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 즐겨찾기 카테고리입니다."));

        return bookmarkRepository.findBookmarkRestaurantsByCategory(userId, bookmarkCategoryId)
                .stream()
                .map(this::toBookmarkRestaurantResponse)
                .toList();
    }

    @Override
    public List<BookmarkCategoryStatusResponse> getBookmarkCategoryStatuses(
            Long userId,
            Long restaurantId
    ) {
        User user = getUser(userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당입니다."));

        List<BookmarkCategory> categories =
                bookmarkCategoryRepository.findAllByUserOrderBySortOrderAscBookmarkCategoryIdAsc(user);

        List<Bookmark> savedBookmarks =
                bookmarkRepository.findAllByUserAndRestaurant(user, restaurant);

        return categories.stream()
                .map(category -> {
                    Bookmark matchedBookmark = savedBookmarks.stream()
                            .filter(bookmark -> bookmark.getBookmarkCategory()
                                    .getBookmarkCategoryId()
                                    .equals(category.getBookmarkCategoryId()))
                            .findFirst()
                            .orElse(null);

                    return new BookmarkCategoryStatusResponse(
                            category.getBookmarkCategoryId(),
                            category.getBookmarkCategoryName(),
                            category.getSortOrder(),
                            category.getIsDefault(),
                            bookmarkRepository.countByBookmarkCategory(category),
                            matchedBookmark != null,
                            matchedBookmark == null ? null : matchedBookmark.getBookmarkId()
                    );
                })
                .toList();
    }

    private BookmarkRestaurantResponse toBookmarkRestaurantResponse(
            BookmarkRestaurantProjection projection
    ) {
        return new BookmarkRestaurantResponse(
                projection.getBookmarkId(),
                projection.getRestaurantId(),
                projection.getRestaurantName(),
                projection.getCategory(),
                projection.getAddress(),
                projection.getImageUrl(),
                projection.getMemo(),
                projection.getCreatedAt(),
                projection.getAverageScore(),
                projection.getReviewCount(),
                parseTags(projection.getTopTags())
        );
    }

    private List<String> parseTags(String topTags) {
        if (topTags == null || topTags.isBlank()) {
            return List.of();
        }

        return Arrays.stream(topTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}