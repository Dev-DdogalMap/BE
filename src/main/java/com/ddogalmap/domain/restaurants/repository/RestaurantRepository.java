package com.ddogalmap.domain.restaurants.repository;

import com.ddogalmap.domain.restaurants.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByManagementNo(String managementNo);
}
