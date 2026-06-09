package com.ddogalmap.domain.bookmarks.service;

import com.ddogalmap.domain.bookmarks.dto.request.CreateBookmarkCategoryRequest;
import com.ddogalmap.domain.bookmarks.dto.request.CreateBookmarkRequest;
import com.ddogalmap.domain.bookmarks.dto.response.BookmarkCategoryResponse;
import com.ddogalmap.domain.bookmarks.dto.response.CreateBookmarkResponse;
import com.ddogalmap.domain.bookmarks.entity.Bookmark;
import com.ddogalmap.domain.bookmarks.entity.BookmarkCategory;
import com.ddogalmap.domain.bookmarks.repository.BookmarkCategoryRepository;
import com.ddogalmap.domain.bookmarks.repository.BookmarkRepository;
import com.ddogalmap.domain.levels.dto.LevelExpEvent;
import com.ddogalmap.domain.levels.enumtype.ActivityType;
import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.restaurants.repository.RestaurantRepository;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkCommandServiceImpl implements BookmarkCommandService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final BookmarkCategoryRepository bookmarkCategoryRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CreateBookmarkResponse createBookmark(Long userId, CreateBookmarkRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당입니다."));

        BookmarkCategory bookmarkCategory = bookmarkCategoryRepository
                .findByBookmarkCategoryIdAndUser(request.bookmarkCategoryId(), user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 즐겨찾기 폴더입니다."));

        if (bookmarkRepository.existsByUserAndRestaurantAndBookmarkCategory(user, restaurant, bookmarkCategory)) {
            throw new IllegalArgumentException("이미 즐겨찾기한 맛집입니다.");
        }

        Bookmark bookmark = new Bookmark(
                user,
                restaurant,
                bookmarkCategory,
                request.memo()
        );

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);

        eventPublisher.publishEvent(new LevelExpEvent(userId, ActivityType.RESTAURANT_BOOKMARK, savedBookmark.getBookmarkId()));

        return new CreateBookmarkResponse(savedBookmark.getBookmarkId());
    }

    @Override
    public void deleteBookmarkFromCategory(Long userId, Long bookmarkCategoryId, Long restaurantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당입니다."));

        BookmarkCategory bookmarkCategory = bookmarkCategoryRepository
                .findByBookmarkCategoryIdAndUser(bookmarkCategoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 즐겨찾기 폴더입니다."));


        Bookmark bookmark = bookmarkRepository
                .findByUserAndRestaurantAndBookmarkCategory(
                        user,
                        restaurant,
                        bookmarkCategory
                )
                .orElseThrow(() -> new IllegalArgumentException("해당 폴더에 저장된 북마크가 없습니다."));

        bookmarkRepository.delete(bookmark);
    }

    @Override
    public BookmarkCategoryResponse createBookmarkCategory(
            Long userId,
            CreateBookmarkCategoryRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String categoryName = request.bookmarkCategoryName() == null
                ? ""
                : request.bookmarkCategoryName().trim();

        if (categoryName.isBlank()) {
            throw new IllegalArgumentException("카테고리명을 입력해주세요.");
        }

        if (categoryName.length() > 100) {
            throw new IllegalArgumentException("카테고리명은 100자 이하로 입력해주세요.");
        }

        if (bookmarkCategoryRepository.existsByUserAndBookmarkCategoryName(user, categoryName)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다.");
        }

        int sortOrder = (int) bookmarkCategoryRepository.countByUser(user);

        BookmarkCategory bookmarkCategory = new BookmarkCategory(
                user,
                categoryName,
                sortOrder,
                false
        );

        BookmarkCategory savedCategory = bookmarkCategoryRepository.save(bookmarkCategory);

        return new BookmarkCategoryResponse(
                savedCategory.getBookmarkCategoryId(),
                savedCategory.getBookmarkCategoryName(),
                savedCategory.getSortOrder(),
                savedCategory.getIsDefault(),
                0L
        );
    }

    @Override
    public void deleteBookmarkCategory(Long userId, Long bookmarkCategoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        BookmarkCategory bookmarkCategory = bookmarkCategoryRepository
                .findByBookmarkCategoryIdAndUser(bookmarkCategoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 즐겨찾기 카테고리입니다."));

        if (bookmarkCategory.getIsDefault()) {
            throw new IllegalArgumentException("기본 카테고리는 삭제할 수 없습니다.");
        }

        bookmarkRepository.deleteAllByBookmarkCategory(bookmarkCategory);
        bookmarkCategoryRepository.delete(bookmarkCategory);
    }
}