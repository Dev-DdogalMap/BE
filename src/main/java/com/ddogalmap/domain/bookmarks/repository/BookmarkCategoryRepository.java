package com.ddogalmap.domain.bookmarks.repository;

import com.ddogalmap.domain.bookmarks.entity.BookmarkCategory;
import com.ddogalmap.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkCategoryRepository extends JpaRepository<BookmarkCategory, Long> {
    List<BookmarkCategory> findAllByUserOrderBySortOrderAscBookmarkCategoryIdAsc(User user);

    Optional<BookmarkCategory> findByBookmarkCategoryIdAndUser(
            Long bookmarkCategoryId,
            User user
    );

    boolean existsByUserAndBookmarkCategoryName(
            User user,
            String bookmarkCategoryName
    );

    long countByUser(User user);


    Optional<BookmarkCategory> findByBookmarkCategoryIdAndUser_UserId(
            Long bookmarkCategoryId,
            Long userId
    );
}
