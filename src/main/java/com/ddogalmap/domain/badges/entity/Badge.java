package com.ddogalmap.domain.badges.entity;

import com.ddogalmap.domain.badges.enumtype.BadgeConditionType;
import com.ddogalmap.domain.users.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "badges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long badgeId;

    @Column(nullable = false)
    private String name;

    @Column(name = "icon_image", nullable = false)
    private String iconImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private BadgeConditionType conditionType;

    @Column(name = "condition_value", nullable = false)
    private int conditionValue;
}
