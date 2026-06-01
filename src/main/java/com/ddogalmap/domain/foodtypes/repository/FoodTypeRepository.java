package com.ddogalmap.domain.foodtypes.repository;

import com.ddogalmap.domain.foodtypes.entity.FoodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodTypeRepository extends JpaRepository<FoodType, Long> {

    Optional<FoodType> findByType(String type);
}
