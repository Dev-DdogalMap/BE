package com.ddogalmap.domain.reviews.entity;

import com.ddogalmap.domain.restaurants.entity.Restaurant;
import com.ddogalmap.domain.reviews.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private Boolean isRevisit = false;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, length = 500)
    private String content;

    // 외래 키 관계 설정
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;



    // 태그 관련 설정
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> tags = new ArrayList<>();
    // 연관관계 편의 메서드
    public void addTag(String tagContent) {
        Tag tag = Tag.builder()
                .content(tagContent)
                .review(this)
                .build();
        this.tags.add(tag);
    }

    // 이미지 관련 설정
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImg> images = new ArrayList<>();
    // 연관관계 편의 메서드
    public void addImage(ReviewImg image) {
        this.images.add(image);
    }

    @Builder
    public Review(Integer score, String content, boolean isRevisit, Long userId, Long restaurantId) {
        this.score = score;
        this.content = content;
        this.isRevisit = isRevisit;
        this.userId = userId;
        this.restaurantId = restaurantId;
    }

    // 연관 관계 매핑 (필요 시 주석 해제)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", insertable = false, updatable = false)
    private Restaurant restaurant;
}