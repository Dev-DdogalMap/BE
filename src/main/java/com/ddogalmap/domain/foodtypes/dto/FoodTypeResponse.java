package com.ddogalmap.domain.foodtypes.dto;

import com.ddogalmap.domain.foodtypes.entity.FoodType;

public record FoodTypeResponse(
        Long foodTypeId,
        String type
) {
    public static FoodTypeResponse from(FoodType ft) {
        return new FoodTypeResponse(ft.getFoodTypeId(), ft.getType());
    }
}
