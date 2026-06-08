package com.ddogalmap.domain.bookmarks.service;

import com.ddogalmap.domain.bookmarks.dto.response.*;
import com.ddogalmap.domain.bookmarks.entity.Bookmark;
import com.ddogalmap.domain.bookmarks.entity.BookmarkCategory;
import com.ddogalmap.domain.bookmarks.repository.BookmarkCategoryRepository;
import com.ddogalmap.domain.bookmarks.repository.BookmarkRepository;
import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.restaurants.repository.RestaurantRepository;
import com.ddogalmap.domain.reviews.entity.ReviewImg;
import com.ddogalmap.domain.reviews.repository.ReviewImgRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkQueryServiceImpl implements BookmarkQueryService {

    private final UserRepository userRepository;
    private final BookmarkCategoryRepository bookmarkCategoryRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReviewImgRepository reviewImgRepository;
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
    public List<BookmarkRestaurantResponse> getMyBookmarks(Long userId) {
        User user = getUser(userId);

        return bookmarkRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toBookmarkRestaurantResponse)
                .toList();
    }

    @Override
    public List<BookmarkRestaurantResponse> getMyBookmarksByCategory(
            Long userId,
            Long bookmarkCategoryId
    ) {
        User user = getUser(userId);

        BookmarkCategory bookmarkCategory = bookmarkCategoryRepository
                .findByBookmarkCategoryIdAndUser(bookmarkCategoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 북마크 카테고리입니다."));

        return bookmarkRepository
                .findAllByUserAndBookmarkCategoryOrderByCreatedAtDesc(user, bookmarkCategory)
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

    private BookmarkRestaurantResponse toBookmarkRestaurantResponse(Bookmark bookmark) {
        Restaurant restaurant = bookmark.getRestaurant();
        String imageUrl = reviewImgRepository
                .findFirstByReview_Restaurant_RestaurantIdOrderByImgIdAsc(
                        restaurant.getRestaurantId()
                )
                .map(ReviewImg::getImgUrl)
                .orElse(null);

        return new BookmarkRestaurantResponse(
                bookmark.getBookmarkId(),
                restaurant.getRestaurantId(),
                restaurant.getPlaceName(),
                restaurant.getFoodType().getType(),
                restaurant.getAddressName(),
                imageUrl,
                bookmark.getMemo(),
                bookmark.getCreatedAt()
        );
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    @Override
    public BookmarkCategoryRestaurantsResponse getBookmarkCategoryRestaurants(Long userId, Long bookmarkCategoryId) {

        log.info(
                "[BookmarkQueryService#getBookmarkCategoryRestaurants] START categoryId={}, userId={}",
                bookmarkCategoryId,
                userId
        );

        User user = getUser(userId);

        BookmarkCategory bookmarkCategory = bookmarkCategoryRepository
                .findByBookmarkCategoryIdAndUser(bookmarkCategoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 북마크 카테고리입니다."));

        List<BookmarkMapRestaurantResponse> restaurants =
                bookmarkRepository
                        .findBookmarkMapRestaurants(
                                userId,
                                bookmarkCategoryId
                        )
                        .stream()
                        .map(restaurant -> new BookmarkMapRestaurantResponse(
                                restaurant.getBookmarkId(),
                                restaurant.getRestaurantId(),
                                restaurant.getPlaceName(),
                                restaurant.getFoodType(),
                                restaurant.getAddressName(),
                                restaurant.getLatitude(),
                                restaurant.getLongitude()
                        ))
                        .toList();

        log.info(
                "[BookmarkQueryService#getBookmarkCategoryRestaurants] END categoryId={}, count={}",
                bookmarkCategoryId,
                restaurants.size()
        );

        return new BookmarkCategoryRestaurantsResponse(
                bookmarkCategory.getBookmarkCategoryId(),
                bookmarkCategory.getBookmarkCategoryName(),
                restaurants.size(),
                restaurants
        );
    }
}