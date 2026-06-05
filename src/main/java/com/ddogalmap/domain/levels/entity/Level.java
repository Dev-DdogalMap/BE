package com.ddogalmap.domain.levels.entity;

import com.ddogalmap.domain.users.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "levels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Level extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long levelId;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer requiredExp;
}
