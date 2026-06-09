package com.ddogalmap.domain.bookmarks.entity;

import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookmark_categories")
public class BookmarkCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_category_id")
    private Long bookmarkCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bookmark_category_name", nullable = false, length = 100)
    private String bookmarkCategoryName;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    public BookmarkCategory(User user, String bookmarkCategoryName, Integer sortOrder, Boolean isDefault) {
        this.user = user;
        this.bookmarkCategoryName = bookmarkCategoryName;
        this.sortOrder = sortOrder;
        this.isDefault = isDefault != null ? isDefault : false;
    }

    public static BookmarkCategory createDefault(User user) {
        return new BookmarkCategory(user, "기본", 0, true);
    }
}