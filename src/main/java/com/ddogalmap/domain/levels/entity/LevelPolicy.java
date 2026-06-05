package com.ddogalmap.domain.levels.entity;

import com.ddogalmap.domain.levels.enumtype.ActivityType;
import com.ddogalmap.domain.users.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "level_policies")
public class LevelPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_policy_id")
    private Long levelPolicyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, unique = true, length = 50)
    private ActivityType activityType;

    @Column(name = "exp", nullable = false)
    private Integer exp;
}
