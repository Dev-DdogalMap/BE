package com.ddogalmap.domain.restaurants.service;

import com.ddogalmap.domain.restaurants.dto.projection.RestaurantSearchProjection;
import com.ddogalmap.domain.restaurants.dto.projection.RestaurantTagProjection;
import com.ddogalmap.domain.restaurants.dto.response.RestaurantSearchResponse;
import com.ddogalmap.domain.restaurants.repository.RestaurantRepository;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 맛집 검색 서비스.
 *
 * 1. RestaurantRepository.searchRestaurants 로 메인 결과 조회 (페이지 단위)
 * 2. 받은 음식점 ID 리스트로 태그를 한 번에 IN 절 조회 (N+1 회피)
 * 3. 자바에서 음식점별 상위 3개 태그 선택
 * 4. 결과 결합해서 Response 반환
 */
@Service
@RequiredArgsConstructor
public class RestaurantSearchService {

    private static final int MAX_TAGS_PER_RESTAURANT = 3;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private static final Set<String> ALLOWED_SORTS = Set.of("distance", "jjinScore", "score");
    private static final String DEFAULT_SORT = "distance";

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public RestaurantSearchResponse search(
            Long currentUserId,
            String keyword,
            String region,
            Long foodTypeId,
            Double lat,
            Double lng,
            String sort,
            int page,
            int size
    ) {
        String normalizedSort = normalizeSort(sort);
        // lat/lng 없으면 distance 정렬 불가 → jjinScore 로 자동 폴백
        if ("distance".equals(normalizedSort) && (lat == null || lng == null)) {
            normalizedSort = "jjinScore";
        }
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (normalizedPage - 1) * normalizedSize;

        String normalizedKeyword = blankToNull(keyword);
        String normalizedRegion = blankToNull(region);

        // region 명시되지 않았고 로그인 사용자라면, 인증된 동네를 자동 적용
        if (normalizedRegion == null && currentUserId != null) {
            normalizedRegion = userRepository.findRegionByUserId(currentUserId)
                    .map(this::blankToNull)
                    .orElse(null);
        }

        long totalCount = restaurantRepository.countSearchRestaurants(
                normalizedKeyword, normalizedRegion, foodTypeId
        );

        List<RestaurantSearchProjection> rows = switch (normalizedSort) {
            case "distance" -> restaurantRepository.searchRestaurantsByDistance(
                    normalizedKeyword, normalizedRegion, foodTypeId,
                    lat, lng, normalizedSize, offset
            );
            case "score" -> restaurantRepository.searchRestaurantsByScore(
                    normalizedKeyword, normalizedRegion, foodTypeId,
                    lat, lng, normalizedSize, offset
            );
            default -> restaurantRepository.searchRestaurantsByJjinScore(
                    normalizedKeyword, normalizedRegion, foodTypeId,
                    lat, lng, normalizedSize, offset
            );
        };

        if (rows.isEmpty()) {
            return new RestaurantSearchResponse(
                    normalizedPage, normalizedSize, totalCount, Collections.emptyList()
            );
        }

        Map<Long, List<String>> tagsByRestaurantId = loadTopTags(rows);

        List<RestaurantSearchResponse.Item> items = new ArrayList<>(rows.size());
        for (RestaurantSearchProjection row : rows) {
            List<String> tags = tagsByRestaurantId.getOrDefault(
                    row.getRestaurantId(), Collections.emptyList()
            );
            items.add(new RestaurantSearchResponse.Item(
                    row.getRestaurantId(),
                    row.getPlaceName(),
                    row.getFoodType(),
                    row.getAddressName(),
                    row.getRoadAddressName(),
                    row.getLatitude(),
                    row.getLongitude(),
                    row.getDistance(),
                    row.getAverageScore(),
                    row.getReviewCount(),
                    row.getJjinScore(),
                    tags
            ));
        }

        return new RestaurantSearchResponse(
                normalizedPage, normalizedSize, totalCount, items
        );
    }

    /**
     * 음식점 ID 리스트로 태그를 한 번에 조회 → 음식점별 상위 3개 선택.
     */
    private Map<Long, List<String>> loadTopTags(List<RestaurantSearchProjection> rows) {
        List<Long> restaurantIds = new ArrayList<>(rows.size());
        for (RestaurantSearchProjection r : rows) {
            restaurantIds.add(r.getRestaurantId());
        }

        List<RestaurantTagProjection> tagRows =
                restaurantRepository.findTagsByRestaurantIds(restaurantIds);

        // 쿼리에서 ORDER BY tagCount DESC 로 이미 정렬되어 옴
        // → 음식점별로 처음 만나는 3개만 담으면 그게 상위 3개
        Map<Long, List<String>> result = new HashMap<>();
        for (RestaurantTagProjection t : tagRows) {
            List<String> tags = result.computeIfAbsent(t.getRestaurantId(), k -> new ArrayList<>());
            if (tags.size() < MAX_TAGS_PER_RESTAURANT) {
                tags.add(t.getTag());
            }
        }
        return result;
    }

    private String normalizeSort(String sort) {
        if (sort == null || !ALLOWED_SORTS.contains(sort)) {
            return DEFAULT_SORT;
        }
        return sort;
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
