package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.dto.response.RestaurantInfoResponse;
import com.ddogalmap.domain.restaurants.dto.response.RestaurantMapResponse;
import com.ddogalmap.domain.restaurants.dto.response.RestaurantPreviewResponse;

public interface RestaurantService {

    RestaurantMapResponse getRestaurantsOnMap(double swLat, double swLng, double neLat, double neLng, int limit);
    RestaurantPreviewResponse getRestaurantPreview(Long restaurantId, double lat, double lng);
    RestaurantInfoResponse getRestaurantInfo(Long restaurantId);
}
