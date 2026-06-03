package com.ddogalmap.domain.reviews.entity;

import com.ddogalmap.domain.reviews.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_imgs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImg extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_id")
    private Long imgId;

    @Column(name = "img_url", nullable = false)
    private String imgUrl;
    @Column(name = "org_img_name", nullable = false)
    private String orgImgName; // 실제 유저가 올린 파일명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Builder
    public ReviewImg(String imgUrl, String orgImgName, Review review) {
        this.imgUrl = imgUrl;
        this.orgImgName = orgImgName;
        this.review = review;
    }
}
