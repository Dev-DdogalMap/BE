package com.ddogalmap.domain.users.entity;

import com.ddogalmap.domain.badges.entity.Badge;
import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.enumtype.UserRole;
import com.ddogalmap.domain.users.enumtype.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private Long kakaoId;

    @Column(length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(length = 100)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime regionVerifiedAt;

    @Column(name = "is_chat_enabled", nullable = false)
    private Boolean chatEnabled = true;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_badge_id")
    private Badge representativeBadge;

    protected User(Long kakaoId, String email, String nickname, String profileImageUrl, String region, UserRole role) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.region = region;
        this.role = role;
    }

    public static User createKakaoUser(Long kakaoId, String email, String nickname, String profileImageUrl) {
        return new User(kakaoId, email, nickname, profileImageUrl, null, UserRole.USER);
    }

    public void updateKakaoProfile(String email, String nickname, String profileImageUrl) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateRegion(String region, LocalDateTime regionVerifiedAt) {
        this.region = region;
        this.regionVerifiedAt = regionVerifiedAt;
    }

    public void updateChatEnabled(Boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    public void withdraw() {
        this.kakaoId = null;
        this.email = null;
        this.nickname = "탈퇴한 사용자";
        this.profileImageUrl = null;
        this.region = null;
        this.status = UserStatus.DELETED;
    }

    public void updateRepresentativeBadge(Badge badge) {
        this.representativeBadge = badge;
    }
}
