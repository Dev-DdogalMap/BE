package com.ddogalmap.domain.regions.service;

import com.ddogalmap.domain.regions.dto.response.RegionTreeResponse;
import com.ddogalmap.domain.regions.dto.response.SigunguResponse;
import com.ddogalmap.domain.regions.entity.Region;
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

        List<Region> regions = regionRepository.findAllByOrderBySidoNameAscSigunguNameAscEupmyeondongNameAsc();

        return regions.stream()
                .collect(Collectors.groupingBy(
                        Region::getSidoName,
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                Region::getSigunguName,
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
                                .map(sigunguEntry -> SigunguResponse.of(
                                        sigunguEntry.getKey(),
                                        sigunguEntry.getValue()
                                ))
                                .toList()
                ))
                .toList();
    }
}
