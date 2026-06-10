package com.ddogalmap.domain.regions.service;

import com.ddogalmap.domain.regions.dto.projection.RegionLiteProjection;
import com.ddogalmap.domain.regions.dto.response.DongResponse;
import com.ddogalmap.domain.regions.dto.response.RegionTreeResponse;
import com.ddogalmap.domain.regions.dto.response.SigunguResponse;
import com.ddogalmap.domain.regions.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    @Override
    public List<RegionTreeResponse> getRegionTree() {

        // geom 컬럼(큰 MultiPolygon) 제외, 5개 컬럼만 SELECT 하는 가벼운 프로젝션 사용
        List<RegionLiteProjection> regions = regionRepository.findAllForTree();

        return regions.stream()
                .collect(Collectors.groupingBy(
                        RegionLiteProjection::getSidoName,
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                RegionLiteProjection::getSigunguName,
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                ))
                .entrySet()
                .stream()
                .map(sidoEntry -> RegionTreeResponse.of(
                        sidoEntry.getKey(),
                        sidoEntry.getValue()
                                .entrySet()
                                .stream()
                                .map(sigunguEntry -> new SigunguResponse(
                                        sigunguEntry.getKey(),
                                        sigunguEntry.getValue().stream()
                                                .map(r -> new DongResponse(
                                                        r.getRegionId(),
                                                        r.getLegalCode(),
                                                        r.getEupmyeondongName()
                                                ))
                                                .toList()
                                ))
                                .toList()
                ))
                .toList();
    }
}
