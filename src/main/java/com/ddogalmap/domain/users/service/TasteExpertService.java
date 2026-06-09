package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.TasteExpertSearchCondition;
import com.ddogalmap.domain.users.dto.response.TasteExpertPageResponse;
import com.ddogalmap.domain.users.enumtype.TasteExpertSortType;
import com.ddogalmap.domain.users.repository.TasteExpertQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TasteExpertService {

    private final TasteExpertQueryRepository tasteExpertQueryRepository;

    @Transactional(readOnly = true)
    public TasteExpertPageResponse getTasteExperts(
            String keyword,
            String region,
            Integer minLevel,
            String sort,
            Integer page,
            Integer size,
            Long currentUserId
    ) {
        int safePage = page == null ? 0 : page;
        int safeSize = size == null ? 20 : size;

        if (safePage < 0) {
            throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        }
        if (safeSize < 1 || safeSize > 100) {
            throw new IllegalArgumentException("size는 1 이상 100 이하여야 합니다.");
        }

        TasteExpertSortType sortType = parseSort(sort);
        TasteExpertSearchCondition condition = new TasteExpertSearchCondition(
                keyword,
                region,
                minLevel,
                currentUserId,
                sortType,
                safePage,
                safeSize
        );
        return tasteExpertQueryRepository.search(condition);
    }

    private TasteExpertSortType parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return TasteExpertSortType.EXPERTISE;
        }
        return TasteExpertSortType.valueOf(sort.trim().toUpperCase());
    }
}
