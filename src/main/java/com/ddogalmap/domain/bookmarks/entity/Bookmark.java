package com.ddogalmap.domain.bookmarks.entity;

import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookmarks")
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_category_id", nullable = false)
    private BookmarkCategory bookmarkCategory;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    public Bookmark(User user, Restaurant restaurant, BookmarkCategory bookmarkCategory, String memo) {
        this.user = user;
        this.restaurant = restaurant;
        this.bookmarkCategory = bookmarkCategory;
        this.memo = memo;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void changeCategory(BookmarkCategory bookmarkCategory) {
        this.bookmarkCategory = bookmarkCategory;
    }
}