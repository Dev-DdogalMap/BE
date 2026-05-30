package com.ddogalmap.domain.foodtypes.entity;

import com.ddogalmap.domain.users.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "food_types")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodTypeId;

    @Column(nullable = false, length = 50)
    private String type;

    protected FoodType(String type) {
        this.type = type;
    }

    public static FoodType create(String type) {
        return new FoodType(type);
    }
}
