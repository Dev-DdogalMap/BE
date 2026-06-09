package com.ddogalmap.domain.levels.entity;

import com.ddogalmap.domain.users.BaseEntity;
import com.ddogalmap.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_levels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userLevelId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @Column(nullable = false)
    private Integer exp;

    public static UserLevel create(
            User user,
            Level level,
            Integer exp
    ) {
        UserLevel userLevel = new UserLevel();

        userLevel.user = user;
        userLevel.level = level;
        userLevel.exp = exp;

        return userLevel;
    }

    public void updateExpAndLevel(Integer exp, Level level) {
        this.exp = exp;
        this.level = level;
    }
}
