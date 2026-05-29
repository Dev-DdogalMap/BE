package com.ddogalmap.domain.foodtypes.service;

import com.ddogalmap.domain.foodtypes.entity.FoodType;
import com.ddogalmap.domain.foodtypes.repository.FoodTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

/**
 * food_types 자동 생성 + 메모리 캐시.
 *
 * 적재 흐름 중 같은 업태가 12만 번 반복 등장하므로,
 * 매번 DB 조회 대신 캐시 hit 으로 처리해 N+1 회피.
 */
@Service
@RequiredArgsConstructor
public class FoodTypeService {

    private static final String DEFAULT_TYPE = "기타";

    private final FoodTypeRepository foodTypeRepository;
    private final ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache() {
        foodTypeRepository.findAll().forEach(ft ->
                cache.put(ft.getType(), ft.getFoodTypeId())
        );
    }

    @Transactional
    public Long getOrCreate(String typeName) {
        String normalized = normalize(typeName);

        Long cached = cache.get(normalized);
        if (cached != null) return cached;

        // 캐시 miss → DB 조회 → 없으면 INSERT
        Long id = foodTypeRepository.findByType(normalized)
                .map(FoodType::getFoodTypeId)
                .orElseGet(() -> foodTypeRepository.save(FoodType.create(normalized)).getFoodTypeId());

        cache.put(normalized, id);
        return id;
    }

    public int getCacheSize() {
        return cache.size();
    }

    private String normalize(String typeName) {
        if (typeName == null) return DEFAULT_TYPE;
        String trimmed = typeName.trim();
        return trimmed.isEmpty() ? DEFAULT_TYPE : trimmed;
    }
}
