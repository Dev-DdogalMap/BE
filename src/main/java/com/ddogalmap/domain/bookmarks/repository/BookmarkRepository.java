package com.ddogalmap.domain.bookmarks.repository;

import com.ddogalmap.domain.bookmarks.entity.Bookmark;
import com.ddogalmap.domain.bookmarks.entity.BookmarkCategory;
import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserOrderByCreatedAtDesc(User user);

    List<Bookmark> findAllByUserAndBookmarkCategoryOrderByCreatedAtDesc(
            User user,
            BookmarkCategory bookmarkCategory
    );

    List<Bookmark> findAllByUserAndRestaurant(User user, Restaurant restaurant);

    long countByBookmarkCategory(BookmarkCategory bookmarkCategory);

    boolean existsByUserAndRestaurantAndBookmarkCategory(User user, Restaurant restaurant, BookmarkCategory bookmarkCategory);

    Optional<Bookmark> findByUserAndRestaurantAndBookmarkCategory(User user, Restaurant restaurant, BookmarkCategory bookmarkCategory);

    void deleteAllByBookmarkCategory(BookmarkCategory bookmarkCategory);
}
