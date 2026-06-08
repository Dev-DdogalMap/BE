package com.ddogalmap.domain.badges.entity;

import com.ddogalmap.domain.foodtypes.entity.FoodType;
import com.ddogalmap.domain.users.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "badge_food_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_badge_food_type",
                        columnNames = {"badge_id", "food_type_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BadgeFoodType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_food_type_id")
    private Long badgeFoodTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_type_id", nullable = false)
    private FoodType foodType;
}