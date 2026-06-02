package com.ddogalmap.domain.foodtypes.controller;

import com.ddogalmap.domain.foodtypes.dto.FoodTypeResponse;
import com.ddogalmap.domain.foodtypes.repository.FoodTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Food Type", description = "음식 종류")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/food-types")
public class FoodTypeController {

    private final FoodTypeRepository foodTypeRepository;

    @Operation(summary = "음식 종류 목록 조회")
    @GetMapping
    public List<FoodTypeResponse> getAll() {
        return foodTypeRepository.findAll().stream()
                .map(FoodTypeResponse::from)
                .toList();
    }
}
